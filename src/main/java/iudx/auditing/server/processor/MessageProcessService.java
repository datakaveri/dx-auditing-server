package iudx.auditing.server.processor;

import io.vertx.codegen.annotations.GenIgnore;
import io.vertx.codegen.annotations.ProxyGen;
import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;

@ProxyGen
@VertxGen
public interface MessageProcessService {

  @GenIgnore
  static MessageProcessService createProxy(Vertx vertx, String address) {
    return new MessageProcessServiceVertxEBProxy(vertx, address);
  }

  Future<JsonObject> processAuditEventMessages(JsonObject message);

  Future<Void> processSubscriptionMonitoringMessages(JsonObject message);
}
