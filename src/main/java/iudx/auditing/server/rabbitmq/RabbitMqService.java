package iudx.auditing.server.rabbitmq;

import io.vertx.codegen.annotations.GenIgnore;
import io.vertx.codegen.annotations.ProxyGen;
import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.Vertx;

@VertxGen
@ProxyGen
public interface RabbitMqService {

  @GenIgnore
  static RabbitMqService createProxy(Vertx vertx, String address) {
    return new RabbitMqServiceVertxEBProxy(vertx, address);
  }
}
