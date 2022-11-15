package iudx.auditing.server.rabbitmq;

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.rabbitmq.QueueOptions;
import io.vertx.rabbitmq.RabbitMQClient;
import io.vertx.rabbitmq.RabbitMQConsumer;
import io.vertx.rabbitmq.RabbitMQOptions;
import iudx.auditing.server.queryStrategy.ServerOrigin;
import iudx.auditing.server.queryStrategy.ServerOriginContextFactory;
import iudx.auditing.server.queryStrategy.ServerStrategy;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class RabbitMQServiceImpl implements RabbitMQService {

  private static final Logger LOGGER = LogManager.getLogger(RabbitMQServiceImpl.class);
  private final RabbitMQClient client;

  private final QueueOptions options =
      new QueueOptions()
          .setMaxInternalQueueSize(1000)
          .setKeepMostRecent(true);

  public RabbitMQServiceImpl(Vertx vertx, RabbitMQOptions options) {
    this.client = RabbitMQClient.create(vertx, options);
    this.client
        .start()
        .onSuccess(handler -> {
          LOGGER.info("RMQ client started.");
        }).onFailure(handler -> {
          LOGGER.fatal("RMQ client startup failed");
        });

  }
  @Override
  public RabbitMQService consume(String queue, Handler<AsyncResult<JsonObject>> handler) {
    client.basicConsumer(queue, options, receivedResultHandler -> {
      if (receivedResultHandler.succeeded()) {
        RabbitMQConsumer mqConsumer = receivedResultHandler.result();
        mqConsumer.handler(message -> {
          JsonObject body = message.body().toJsonObject();
          if (body != null) {

            String origin = body.getString("origin");
            ServerOrigin serverOrigin = ServerOrigin.fromRole(origin);
            ServerOriginContextFactory serverOriginContextFactory= new ServerOriginContextFactory();
            ServerStrategy serverStrategy=serverOriginContextFactory.create(serverOrigin);
            String query= serverStrategy.buildWriteQuery(body);

            handler.handle(Future.succeededFuture(new JsonObject("body")));
          } else {
            handler.handle(Future.failedFuture("null/empty message"));
          }
        });
      }
    });

    return this;
  }

}
