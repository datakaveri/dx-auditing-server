package iudx.auditing.server.rabbitmq.consumers;

import static iudx.auditing.server.common.Constants.SUBSCRIPTION_MONITORING_QUEUE;

import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.DecodeException;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.rabbitmq.QueueOptions;
import io.vertx.rabbitmq.RabbitMQClient;
import io.vertx.rabbitmq.RabbitMQConsumer;
import io.vertx.rabbitmq.RabbitMQOptions;
import iudx.auditing.server.common.RabitMqConsumer;
import iudx.auditing.server.processor.MessageProcessService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class SubscriptionMonitoringConsumer implements RabitMqConsumer {

  private static final Logger LOGGER = LogManager.getLogger(SubscriptionMonitoringConsumer.class);

  private final RabbitMQClient client;
  private final MessageProcessService msgService;

  private final QueueOptions options =
      new QueueOptions().setKeepMostRecent(true).setMaxInternalQueueSize(1000).setAutoAck(false);

  public SubscriptionMonitoringConsumer(
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
                  SUBSCRIPTION_MONITORING_QUEUE,
                  options,
                  receiveResultHandler -> {
                    if (receiveResultHandler.succeeded()) {
                      RabbitMQConsumer mqConsumer = receiveResultHandler.result();
                      mqConsumer.handler(
                          message -> {
                            long deliveryTag = message.envelope().getDeliveryTag();
                            Buffer body = message.body();
                            if (body != null) {
                              LOGGER.info("Subscription message received");
                              boolean isArrayReceived = isJsonArray(body);
                              LOGGER.debug("is message array received : {}", isArrayReceived);
                              if (isArrayReceived) {
                                JsonArray jsonArrayBody = body.toJsonArray();
                                jsonArrayBody.forEach(
                                    json -> {
                                      Future.future(e -> messagePush((JsonObject) json));
                                    });
                                client.basicAck(deliveryTag, false);
                              } else {
                                messagePush(new JsonObject(body))
                                    .onSuccess(
                                        successResult -> {
                                          LOGGER.info("Latest message published in RMQ.");
                                          client.basicAck(deliveryTag, false);
                                        })
                                    .onFailure(
                                        failureHandler -> {
                                          LOGGER.warn(
                                              "Error while publishing messages for processing "
                                                  + failureHandler.getMessage());
                                          if (failureHandler
                                              .getMessage()
                                              .matches(
                                                  "(.*)Subscriber doesn't exist/expired(.*)")) {
                                            client.basicAck(deliveryTag, false);
                                          }
                                        });
                              }
                            }
                          });
                    }
                  });
            })
        .onFailure(
            failureHandler -> {
              LOGGER.fatal("Rabbit client startup failed for subscription message Q consumer.");
            });
  }

  public boolean isJsonArray(Buffer jsonObjectBuffer) {
    Object value;
    try {
      value = jsonObjectBuffer.toJson();
    } catch (DecodeException e) {
      value = false;
      LOGGER.error("Error while decoding the message");
      throw new RuntimeException(e);
    }
    LOGGER.debug("isArray : {}", value instanceof JsonArray);
    return value instanceof JsonArray;
  }

  public Future<Void> messagePush(JsonObject json) {
    Promise<Void> promise = Promise.promise();
    msgService
        .processSubscriptionMonitoringMessages(json)
        .onSuccess(
            processResult -> {
              LOGGER.debug("Subscription message published for processing");
              promise.complete();
            })
        .onFailure(
            processFailure -> {
              LOGGER.error(processFailure.getMessage());
              promise.fail(processFailure.getMessage());
            });
    return promise.future();
  }
}
