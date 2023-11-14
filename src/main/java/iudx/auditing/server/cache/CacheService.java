package iudx.auditing.server.cache;

import io.vertx.codegen.annotations.GenIgnore;
import io.vertx.codegen.annotations.ProxyGen;
import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;

@ProxyGen
@VertxGen
public interface CacheService {

  @GenIgnore
  static CacheService createProxy(Vertx vertx, String address) {
    return new CacheServiceVertxEBProxy(vertx, address);
  }

  Future<JsonObject> get(String key);

  Future<Void> refreshCache();
}
