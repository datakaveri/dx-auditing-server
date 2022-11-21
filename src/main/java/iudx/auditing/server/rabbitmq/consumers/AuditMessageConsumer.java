package iudx.auditing.server.rabbitmq.consumers;

import static iudx.auditing.server.common.Constants.AUDIT_LATEST_QUEUE;
import static iudx.auditing.server.common.Constants.DELIVERY_TAG;

import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.rabbitmq.QueueOptions;
import io.vertx.rabbitmq.RabbitMQClient;
import io.vertx.rabbitmq.RabbitMQConsumer;
import io.vertx.rabbitmq.RabbitMQOptions;
import iudx.auditing.server.common.IConsumer;
import iudx.auditing.server.processor.MessageProcessService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class AuditMessageConsumer implements IConsumer {

  private static final Logger LOGGER = LogManager.getLogger(AuditMessageConsumer.class);

  private final RabbitMQClient client;
  private final MessageProcessService msgService;
  private final Vertx vertx;

  private final QueueOptions options =
      new QueueOptions().setKeepMostRecent(true).setMaxInternalQueueSize(100).setAutoAck(false);

  public AuditMessageConsumer(
      Vertx vertx, RabbitMQOptions options, MessageProcessService msgService) {
    this.vertx = vertx;
    this.client = RabbitMQClient.create(vertx, options);
    this.msgService = msgService;
  }

  @Override
  public void start() {
    this.consume();
  }

  private void consume() {
    client.start().onSuccess(successHandler -> {
      client.basicConsumer(AUDIT_LATEST_QUEUE, options, receiveResultHandler -> {
        if (receiveResultHandler.succeeded()) {
          RabbitMQConsumer mqConsumer = receiveResultHandler.result();
          mqConsumer.handler(message -> {
            mqConsumer.pause();
            LOGGER.debug("message consumption paused.");
            long deliveryTag = message.envelope().getDeliveryTag();
            JsonObject request = message.body().toJsonObject().put(DELIVERY_TAG, deliveryTag);
            Future<JsonObject> processResult = msgService.process(request);
            processResult.onComplete(handler -> {
              if (handler.succeeded()) {
                LOGGER.debug("Latest message published in databases ");
                client.basicAck(handler.result().getLong(DELIVERY_TAG), false);
                mqConsumer.resume();
                LOGGER.debug("message consumption resumed");
              } else {
                LOGGER.error("Error while publishing messages for processing");
                mqConsumer.resume();
                LOGGER.debug("message consumption resumed");
              }
            });
          });
        }
      });
    }).onFailure(failureHandler -> {
      LOGGER.fatal("Rabbit client startup failed for Latest message Q consumer.");
    });
  }
}
