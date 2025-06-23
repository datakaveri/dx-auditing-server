package org.cdpg.dx.databroker.listeners;

import io.vertx.core.json.JsonObject;
import io.vertx.rabbitmq.QueueOptions;
import io.vertx.rabbitmq.RabbitMQClient;
import io.vertx.rabbitmq.RabbitMQConsumer;
import io.vertx.rabbitmq.RabbitMQMessage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cdpg.dx.auditingserver.activity.immudbActivity.ImmudbActivityService;
import org.cdpg.dx.auditingserver.activity.model.ImmudbActivityLog;

public class ImmudbConsumer implements RabitMqConsumer {

  private static final Logger LOGGER = LogManager.getLogger(ImmudbConsumer.class);
  private static final String QUEUE_NAME = "immudb-msg";
  static int IMMUDB_COUNTER = 0;
  private final RabbitMQClient rabbitMqClient;
  private final ImmudbActivityService activityService;
  private final QueueOptions options =
      new QueueOptions().setMaxInternalQueueSize(100).setKeepMostRecent(true).setAutoAck(false);
  private ImmudbActivityLog immudbActivityLog;

  public ImmudbConsumer(RabbitMQClient rabbitMqClient, ImmudbActivityService activityService) {
    this.rabbitMqClient = rabbitMqClient;
    this.activityService = activityService;
  }

  @Override
  public void start() {
    consume();
  }

  private void consume() {
    rabbitMqClient
        .start()
        .onSuccess(
            v ->
                rabbitMqClient.basicConsumer(
                    QUEUE_NAME,
                    options,
                    result -> {
                      if (result.succeeded()) {
                        RabbitMQConsumer mqConsumer = result.result();
                        mqConsumer.handler(this::handleMessage);
                      } else {
                        LOGGER.error(
                            "Failed to consume from {}: {}",
                            QUEUE_NAME,
                            result.cause().getMessage());
                      }
                    }))
        .onFailure(
            failure -> LOGGER.fatal("Rabbit client startup failed for {} Q consumer.", QUEUE_NAME));
  }

  private void handleMessage(RabbitMQMessage message) {
    LOGGER.info("Consuming message: {}", message.body());
    long deliveryTag = message.envelope().getDeliveryTag();
    JsonObject json = message.body().toJsonObject();
    try {
      immudbActivityLog = ImmudbActivityLog.fromJson(json);
      // proceed with activityLogEntity
    } catch (Exception e) {
      LOGGER.error("Failed to parse ImmudbActivityLog from JSON: {}", e.getMessage());
      rabbitMqClient.basicAck(deliveryTag, false);
    }

    activityService
        .insertActivityLogIntoImmudb(immudbActivityLog)
        .onSuccess(
            v -> {
              LOGGER.info("Activity log inserted into Immudb successfully.");
              rabbitMqClient.basicAck(deliveryTag, false);
            })
        .onFailure(
            err -> {
              LOGGER.error("Error inserting activity log into Immudb: {}", err.getMessage());
              if (IMMUDB_COUNTER < 6) {
                rabbitMqClient.basicNack(deliveryTag, false, true);
                try {
                  Thread.sleep(5000);
                } catch (InterruptedException e) {
                  throw new RuntimeException(e);
                }
                IMMUDB_COUNTER++;
              } else {
                LOGGER.debug("sending message to dead letter queue");
                rabbitMqClient.basicNack(deliveryTag, false, false);
                IMMUDB_COUNTER = 0;
              }
            });
  }
}
