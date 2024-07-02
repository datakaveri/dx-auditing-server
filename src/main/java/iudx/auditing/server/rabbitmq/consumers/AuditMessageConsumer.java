package iudx.auditing.server.rabbitmq.consumers;

import static iudx.auditing.server.common.Constants.*;
import static iudx.auditing.server.querystrategy.util.Constants.ERROR_UNIQUE_KEY;

import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.rabbitmq.QueueOptions;
import io.vertx.rabbitmq.RabbitMQClient;
import io.vertx.rabbitmq.RabbitMQConsumer;
import io.vertx.rabbitmq.RabbitMQOptions;
import iudx.auditing.server.common.RabitMqConsumer;
import iudx.auditing.server.processor.MessageProcessService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class AuditMessageConsumer implements RabitMqConsumer {

  private static final Logger LOGGER = LogManager.getLogger(AuditMessageConsumer.class);
  public static long successCount;
  public static long totalCount;
  private final RabbitMQClient client;
  private final MessageProcessService msgService;
  private final QueueOptions options =
      new QueueOptions().setMaxInternalQueueSize(100).setKeepMostRecent(true).setAutoAck(false);

  public AuditMessageConsumer(
      Vertx vertx, RabbitMQOptions options, MessageProcessService msgService) {
    this.client = RabbitMQClient.create(vertx, options);
    this.msgService = msgService;
  }

  @Override
  public void start() {
    this.consume();
  }

  private void consume() {
    client
        .start()
        .onSuccess(
            successHandler -> {
              client
                  .basicQos(0)
                  .onSuccess(
                      qosHandler -> {
                        client.basicConsumer(
                            AUDIT_LATEST_QUEUE,
                            options,
                            receiveResultHandler -> {
                              if (receiveResultHandler.succeeded()) {
                                RabbitMQConsumer mqConsumer = receiveResultHandler.result();
                                mqConsumer.handler(
                                    message -> {
                                      long startTime = System.currentTimeMillis();
                                      mqConsumer.pause();
                                      LOGGER.debug("message consumption paused.");
                                      JsonObject request = new JsonObject();
                                      long deliveryTag = message.envelope().getDeliveryTag();
                                      try {
                                        request =
                                            message
                                                .body()
                                                .toJsonObject()
                                                .put(DELIVERY_TAG, deliveryTag);
                                        LOGGER.info(
                                            "message received from {}", request.getString(ORIGIN));
                                        Future<JsonObject> processResult =
                                            msgService.processAuditEventMessages(request);
                                        processResult.onComplete(
                                            handler -> {
                                              if (handler.succeeded()) {
                                                long endTime =
                                                    System.currentTimeMillis(); // End time
                                                long duration =
                                                    endTime - startTime; // Time difference
                                                LOGGER.info(
                                                    "Audit message published in databases. Time taken: "
                                                        + duration
                                                        + " ms");
                                                client.basicAck(
                                                    handler.result().getLong(DELIVERY_TAG), false);
                                                mqConsumer.resume();
                                                LOGGER.debug("message consumption resumed");
                                                successCount++;
                                                LOGGER.info(" success count == " + successCount);
                                              } else {
                                                LOGGER.error(
                                                    "Error while publishing messages for processing "
                                                        + handler.cause().getMessage());
                                                if (handler
                                                    .cause()
                                                    .getMessage()
                                                    .matches(ERROR_UNIQUE_KEY)) {
                                                  client.basicAck(deliveryTag, false);
                                                } else {
                                                  LOGGER.debug("trying requeue");
                                                  client.basicNack(deliveryTag, false, true);
                                                }
                                                mqConsumer.resume();
                                                LOGGER.debug("message consumption resumed");
                                              }
                                            });
                                      } catch (Exception e) {
                                        LOGGER.error("Error while decoding the message");
                                        LOGGER.debug("trying requeue");
                                        client.basicNack(deliveryTag, false, true);
                                        mqConsumer.resume();
                                        LOGGER.debug("message consumption resumed");
                                      }
                                      totalCount++;
                                      LOGGER.info(" total count == " + totalCount);
                                    });
                              } else {
                                LOGGER.error(
                                    "failed to consume message from auditing-messages Q : {}",
                                    receiveResultHandler.cause().getMessage());
                              }
                            });
                      })
                  .onFailure(
                      qosFailure -> {
                        LOGGER.error("Failed to set QoS (prefetch count) :", qosFailure);
                      });
            })
        .onFailure(
            failureHandler -> {
              LOGGER.fatal("Rabbit client startup failed for auditing-messages Q consumer.");
            });
  }
}
