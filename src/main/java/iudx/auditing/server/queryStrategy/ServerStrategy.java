package iudx.auditing.server.queryStrategy;

import io.vertx.core.json.JsonObject;

public interface ServerStrategy {
  String buildWriteQuery(JsonObject request);
}
