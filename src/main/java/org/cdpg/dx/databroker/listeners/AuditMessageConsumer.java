package org.cdpg.dx.databroker.listeners;

import io.vertx.core.json.JsonObject;
import io.vertx.rabbitmq.QueueOptions;
import io.vertx.rabbitmq.RabbitMQClient;
import io.vertx.rabbitmq.RabbitMQConsumer;
import io.vertx.rabbitmq.RabbitMQMessage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cdpg.dx.auditingserver.activity.model.ActivityLog;
import org.cdpg.dx.auditingserver.activity.service.ActivityService;

public class AuditMessageConsumer implements RabitMqConsumer {

  private static final Logger LOGGER = LogManager.getLogger(AuditMessageConsumer.class);
  private static final String QUEUE_NAME = "test-auditing";
  static int PG_COUNTER = 0;
  private final RabbitMQClient rabbitMqClient;
  private final ActivityService activityService;
  private final QueueOptions options =
      new QueueOptions().setMaxInternalQueueSize(100).setKeepMostRecent(true).setAutoAck(false);

  public AuditMessageConsumer(RabbitMQClient rabbitMqClient, ActivityService activityService) {
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
    ActivityLog activityLogEntity = ActivityLog.fromJson(json);

    activityService
        .insertActivityLogIntoDb(activityLogEntity)
        .onSuccess(
            v -> {
              LOGGER.info("Activity log inserted successfully.");
              rabbitMqClient.basicAck(deliveryTag, false); // Only ack on success
            })
        .onFailure(
            err -> {
              if (err.getMessage() != null && err.getMessage().contains("duplicate key")) {
                LOGGER.warn("Duplicate key error. Ignoring message.");
                rabbitMqClient.basicAck(deliveryTag, false); // Ack for duplicate key to avoid retry
              } else {
                LOGGER.error("Error inserting activity log: {}", err.getMessage());
                if (PG_COUNTER < 6) {
                  rabbitMqClient.basicNack(deliveryTag, false, true);
                  try {
                    Thread.sleep(5000);
                  } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                  }
                  PG_COUNTER++;
                } else {
                  LOGGER.debug("sending message to dead letter queue");
                  rabbitMqClient.basicNack(deliveryTag, false, false);
                  PG_COUNTER = 0;
                }
              }
            });
  }
}
