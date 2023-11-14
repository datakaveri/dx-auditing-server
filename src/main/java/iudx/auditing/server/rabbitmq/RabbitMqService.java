package iudx.auditing.server.rabbitmq;

import io.vertx.codegen.annotations.GenIgnore;
import io.vertx.codegen.annotations.ProxyGen;
import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;

@VertxGen
@ProxyGen
public interface RabbitMqService {

  @GenIgnore
  static RabbitMqService createProxy(Vertx vertx, String address) {
    return new RabbitMqServiceVertxEBProxy(vertx, address);
  }

  Future<Void> publishMessage(JsonObject request);
}
