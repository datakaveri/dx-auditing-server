package iudx.auditing.server.immuDb;

import io.vertx.codegen.annotations.Fluent;
import io.vertx.codegen.annotations.GenIgnore;
import io.vertx.codegen.annotations.ProxyGen;
import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;

@VertxGen
@ProxyGen
public interface ImmuDbService {

    Future<JsonObject> executeWriteQuery(final String query);


    static ImmuDbService createProxy(Vertx vertx, String address) {
        return new ImmuDbServiceVertxEBProxy(vertx, address);
    }

}
