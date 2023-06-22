package iudx.auditing.server.querystrategy;

import io.vertx.core.json.JsonObject;

import static iudx.auditing.server.querystrategy.util.Constants.*;

public class RsSubscriptionStrategy implements AuditingServerStrategy {
  private final JsonObject config;

  public RsSubscriptionStrategy(JsonObject config) {
    this.config = config;
  }

  @Override
  public String buildPostgresWriteQuery(JsonObject request) {
    String eventType = request.getString("eventType");

    switch (eventType) {
      case "SUBS_UPDATED":
      case "SUBS_APPEND":
        return updateQuery(request, eventType);
      case "SUBS_CREATED":
        return createQuery(request);
      default:
        return null;
    }
  }

  private String createQuery(JsonObject request) {
    String subscriptionId = request.getString("subscriptionID");
    String userId = request.getString("userid");
    String subscriptionType = request.getString("subscriptionType");
    String resource = request.getString("resource");
    String eventType = request.getString("eventType");
    String databaseTableName = this.config.getString(RS_SUBS_TABLE_NAME);

    return RS_SUBS_WRITE_QUERY_PG
        .replace("$0", databaseTableName)
        .replace("$1", subscriptionId)
        .replace("$2", userId)
        .replace("$3", eventType)
        .replace("$4", subscriptionType)
        .replace("$5", resource);
  }

  private String updateQuery(JsonObject request, String eventType) {
    String subscriptionId = request.getString("subscriptionID");
    String databaseTableName = this.config.getString(RS_SUBS_TABLE_NAME);
    return RS_SUBS_UPDATE_QUERY_PG
        .replace("$0",databaseTableName)
        .replace("$1", eventType)
        .replace("$2", subscriptionId);
  }

  @Override
  public String buildPostgresDeleteQuery(JsonObject request) {
    if (request.getString("eventType").equals("SUBS_DELETED")) {
      String subscriptionId = request.getString("subscriptionID");
      String databaseTableName = this.config.getString(RS_SUBS_TABLE_NAME);

      return DELETE_SUBSCRIPTION_QUERY
          .replace("$0", databaseTableName)
          .replace("$1", subscriptionId);
    }
    return null;
  }

  @Override
  public String buildImmudbWriteQuery(JsonObject request) {
    return null;
  }
}
