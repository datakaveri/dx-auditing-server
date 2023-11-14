package iudx.auditing.server.rabbitmq;

import static iudx.auditing.server.common.Constants.EXCHANGE_NAME;
import static iudx.auditing.server.common.Constants.ROUTING_KEY;

import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
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

  public Future<Void> publishMessage(JsonObject request) {
    Promise<Void> promise = Promise.promise();
    LOGGER.debug("Sending message to exchange: {}, with routing key: {}", EXCHANGE_NAME,
        ROUTING_KEY);
    client.basicPublish(EXCHANGE_NAME, ROUTING_KEY, request.toBuffer(),
        asyncResult -> {
          if (asyncResult.succeeded()) {
            LOGGER.debug("messeage pushed into the the dataBroker");
            promise.complete();
          } else {
            LOGGER.debug("Failed to push messeage into dataBroker");
            promise.fail(asyncResult.cause());
          }
        });
    return promise.future();
  }
}
