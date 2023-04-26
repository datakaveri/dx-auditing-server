package iudx.auditing.server.rabbitmq;

import io.vertx.core.Vertx;
import io.vertx.rabbitmq.RabbitMQClient;
import io.vertx.rabbitmq.RabbitMQOptions;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class RabbitMqServiceImpl implements RabbitMqService {

  private static final Logger LOGGER = LogManager.getLogger(RabbitMqServiceImpl.class);
  private final RabbitMQClient client;

  public RabbitMqServiceImpl(Vertx vertx, RabbitMQOptions options) {
    this.client = RabbitMQClient.create(vertx, options);
    this.client
        .start()
        .onSuccess(
            handler -> {
              LOGGER.info("RMQ client started.");
            })
        .onFailure(
            handler -> {
              LOGGER.fatal("RMQ client startup failed");
            });
  }
}
