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
  private static final String QUEUE_NAME = "audit-msg";
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
                    "audit-msg",
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
    ActivityLog activityLogEntity = null;
   try {
         activityLogEntity = ActivityLog.fromJson(json);
        // proceed with activityLogEntity
    } catch (Exception e) {
        LOGGER.error("Failed to parse ActivityLog from JSON: {}", e.getMessage());
        ackMessage(deliveryTag); // Optionally ack to avoid retrying bad messages
    }

    activityService
        .insertActivityLogIntoDb(activityLogEntity)
        .onSuccess(
            v -> {
              LOGGER.info("Activity log inserted successfully.");
              ackMessage(deliveryTag); // Only ack on success
            })
        .onFailure(
            err -> {
              if (err.getMessage() != null && err.getMessage().contains("duplicate key")) {
                LOGGER.warn("Duplicate key error. Ignoring message.");
                ackMessage(deliveryTag); // Ack for duplicate key to avoid retry
              } else {
                LOGGER.error("Error inserting activity log: {}", err.getMessage());
                // Do NOT ack here; message will remain in queue for retry
              }
            });
  }

  private void ackMessage(long deliveryTag) {
    rabbitMqClient.basicAck(deliveryTag, false);
  }
}
