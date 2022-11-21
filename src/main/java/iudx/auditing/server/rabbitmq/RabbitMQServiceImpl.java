package iudx.auditing.server.rabbitmq;

import io.vertx.core.Vertx;
import io.vertx.rabbitmq.QueueOptions;
import io.vertx.rabbitmq.RabbitMQClient;
import io.vertx.rabbitmq.RabbitMQOptions;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class RabbitMQServiceImpl implements RabbitMQService {

  private static final Logger LOGGER = LogManager.getLogger(RabbitMQServiceImpl.class);
  private final RabbitMQClient client;

  public RabbitMQServiceImpl(Vertx vertx, RabbitMQOptions options) {
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
