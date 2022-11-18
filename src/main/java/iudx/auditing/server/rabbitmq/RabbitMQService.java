package iudx.auditing.server.rabbitmq;

import io.vertx.codegen.annotations.GenIgnore;
import io.vertx.codegen.annotations.ProxyGen;
import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.Vertx;

@VertxGen
@ProxyGen
public interface RabbitMQService {

  @GenIgnore
  static RabbitMQService createProxy(Vertx vertx, String address) {
    return new RabbitMQServiceVertxEBProxy(vertx, address);
  }
}
