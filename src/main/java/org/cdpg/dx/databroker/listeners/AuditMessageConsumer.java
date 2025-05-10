package org.cdpg.dx.databroker.listeners;

import io.vertx.core.json.JsonObject;
import io.vertx.rabbitmq.QueueOptions;
import io.vertx.rabbitmq.RabbitMQClient;
import io.vertx.rabbitmq.RabbitMQConsumer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cdpg.dx.auditing.activity.model.ActivityLog;
import org.cdpg.dx.auditing.activity.service.ActivityService;

public class AuditMessageConsumer implements RabitMqConsumer {

  private static final Logger LOGGER = LogManager.getLogger(AuditMessageConsumer.class);
  private final RabbitMQClient iudxInternalRabbitMqClient;
  private final ActivityService activityService;
  private final QueueOptions options =
      new QueueOptions().setMaxInternalQueueSize(100).setKeepMostRecent(true).setAutoAck(false);

  public AuditMessageConsumer(
      RabbitMQClient iudxInternalRabbitMqClient, ActivityService activityService) {
    this.iudxInternalRabbitMqClient = iudxInternalRabbitMqClient;
    this.activityService = activityService;
  }

  @Override
  public void start() {
    this.consume();
  }

  private void consume() {
    iudxInternalRabbitMqClient
        .start()
        .onSuccess(
            successHandler -> {
              iudxInternalRabbitMqClient.basicConsumer(
                  "test-auditing",
                  options,
                  receiveResultHandler -> {
                    if (receiveResultHandler.succeeded()) {
                      RabbitMQConsumer mqConsumer = receiveResultHandler.result();
                      mqConsumer.handler(
                          message -> {
                            LOGGER.info("Consuming message: {}", message.body().toString());
                            long deliveryTag = message.envelope().getDeliveryTag();
                            JsonObject json = message.body().toJsonObject();
                            ActivityLog activityLog = ActivityLog.fromJson(json);

                            activityService
                                .insertActivityLogIntoDb(activityLog)
                                .onSuccess(
                                    v -> {
                                      LOGGER.info("Activity log inserted successfully.");
                                      iudxInternalRabbitMqClient.basicAck(deliveryTag, false);
                                    })
                                .onFailure(
                                    err -> {
                                      if (err.getMessage().contains("duplicate key")) {
                                        LOGGER.warn("Duplicate key error. Ignoring message.");
                                        iudxInternalRabbitMqClient.basicAck(deliveryTag, false);
                                      } else {
                                        LOGGER.error(
                                            "Error inserting activity log: {}", err.getMessage());
                                        iudxInternalRabbitMqClient.basicAck(deliveryTag, false);
                                      }
                                    });
                          });
                    } else {
                      LOGGER.error(
                          "Failed to consume message from auditing-messages Q: {}",
                          receiveResultHandler.cause().getMessage());
                    }
                  });
            })
        .onFailure(
            failureHandler -> {
              LOGGER.fatal("Rabbit client startup failed for auditing-messages Q consumer.");
            });
  }
}
