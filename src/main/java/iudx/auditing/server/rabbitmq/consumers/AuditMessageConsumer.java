package iudx.auditing.server.rabbitmq.consumers;

import static iudx.auditing.server.common.Constants.*;

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

  private final RabbitMQClient client;
  private final MessageProcessService msgService;
  public static long count;

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
              client.basicConsumer(
                  AUDIT_LATEST_QUEUE,
                  options,
                  receiveResultHandler -> {
                    if (receiveResultHandler.succeeded()) {
                      RabbitMQConsumer mqConsumer = receiveResultHandler.result();
                      mqConsumer.handler(
                          message -> {
                            mqConsumer.pause();
                            LOGGER.debug("message consumption paused.");
                            JsonObject request = new JsonObject();
                            try {
                              long deliveryTag = message.envelope().getDeliveryTag();
                              request =
                                  message.body().toJsonObject().put(DELIVERY_TAG, deliveryTag);
                              LOGGER.info("message received from {}", request.getString(ORIGIN));
                              Thread.sleep(10000);
                              client.basicAck(deliveryTag, false);
                              mqConsumer.resume();
                              LOGGER.debug("message consumption resumed");
                              count++;
                              LOGGER.info("count == " + count);

                              /* Future<JsonObject> processResult =
                                  msgService.processAuditEventMessages(request);
                              processResult.onComplete(
                                  handler -> {
                                    if (handler.succeeded()) {
                                      LOGGER.info("Audit message published in databases.");
                                      client.basicAck(
                                          handler.result().getLong(DELIVERY_TAG), false);
                                      // mqConsumer.resume();
                                      LOGGER.debug("message consumption resumed");
                                    } else {
                                      LOGGER.error(
                                          "Error while publishing messages for processing "
                                              + handler.cause().getMessage());
                                      //  mqConsumer.resume();
                                      LOGGER.debug("message consumption resumed");
                                    }
                                  });*/
                            } catch (Exception e) {
                              LOGGER.error("Error while decoding the message");
                              // mqConsumer.resume();
                              LOGGER.debug("message consumption resumed");
                            }
                          });
                    } else {
                      LOGGER.error(
                          "failed to consume message from auditing-messages Q : {}",
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
