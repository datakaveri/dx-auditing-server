package iudx.auditing.server.queryStrategy;

import io.vertx.core.json.JsonObject;

public interface ServerStrategy {
  String buildPostgresWriteQuery(JsonObject request);

  String buildPostgresDeleteQuery(JsonObject request);

  String buildImmudbDeleteQuery(JsonObject request);
}
