package iudx.auditing.server.postgres;

import io.vertx.codegen.annotations.GenIgnore;
import io.vertx.codegen.annotations.ProxyGen;
import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

@VertxGen
@ProxyGen
public interface PostgresService {

  @GenIgnore
  static PostgresService createProxy(Vertx vertx, String address) {
    return new PostgresServiceVertxEBProxy(vertx, address);
  }

  Future<JsonObject> executeWriteQuery(final JsonObject query);

  Future<JsonObject> executeDeleteQuery(final JsonObject query);

  Future<JsonArray> executeReadQuery(final String query);
}
