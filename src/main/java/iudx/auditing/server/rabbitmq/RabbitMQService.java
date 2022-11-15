package iudx.auditing.server.rabbitmq;

import io.vertx.codegen.annotations.Fluent;
import io.vertx.codegen.annotations.GenIgnore;
import io.vertx.codegen.annotations.ProxyGen;
import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;

@VertxGen
@ProxyGen
public interface RabbitMQService {

  @Fluent
  RabbitMQService consume(String queue, Handler<AsyncResult<JsonObject>> handler);

  @GenIgnore
  static RabbitMQService createProxy(Vertx vertx, String address) {
    return new RabbitMQServiceVertxEBProxy(vertx, address);
  }

}
