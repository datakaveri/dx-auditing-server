package org.cdpg.dx.databroker.listeners;

import static iudx.auditing.server.common.Constants.*;

import io.vertx.rabbitmq.QueueOptions;
import io.vertx.rabbitmq.RabbitMQClient;
import io.vertx.rabbitmq.RabbitMQConsumer;
import iudx.auditing.server.common.RabitMqConsumer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class AuditMessageConsumer implements RabitMqConsumer {

  private static final Logger LOGGER = LogManager.getLogger(AuditMessageConsumer.class);
  private final RabbitMQClient iudxInternalRabbitMqClient;
  private final QueueOptions options =
      new QueueOptions().setMaxInternalQueueSize(100).setKeepMostRecent(true).setAutoAck(false);

  public AuditMessageConsumer(RabbitMQClient iudxInternalRabbitMqClient) {
    this.iudxInternalRabbitMqClient = iudxInternalRabbitMqClient;
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
                  AUDIT_LATEST_QUEUE,
                  options,
                  receiveResultHandler -> {
                    if (receiveResultHandler.succeeded()) {
                      RabbitMQConsumer mqConsumer = receiveResultHandler.result();
                      mqConsumer.handler(message -> {
                        LOGGER.info("Consuming messaging");
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
