package iudx.auditing.server.postgres;

import io.vertx.codegen.annotations.Fluent;
import io.vertx.codegen.annotations.GenIgnore;
import io.vertx.codegen.annotations.ProxyGen;
import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;

@VertxGen
@ProxyGen
public interface PostgresService {


    Future<JsonObject> executeWriteQuery(final JsonObject query);

    Future<JsonObject> executeDeleteQuery(final JsonObject query);

    @GenIgnore
    static PostgresService createProxy(Vertx vertx, String address){
        return new PostgresServiceVertxEBProxy(vertx, address);
    }
}
