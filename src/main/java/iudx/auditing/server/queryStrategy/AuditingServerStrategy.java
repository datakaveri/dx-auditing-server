package iudx.auditing.server.queryStrategy;

import io.vertx.core.json.JsonObject;

public interface AuditingServerStrategy {
  String buildPostgresWriteQuery(JsonObject request);

  String buildPostgresDeleteQuery(JsonObject request);

  String buildImmudbWriteQuery(JsonObject request);
}
