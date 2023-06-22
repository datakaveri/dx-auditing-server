package iudx.auditing.server.querystrategy;

import static iudx.auditing.server.querystrategy.util.Constants.RS_SUBS_TABLE_NAME;
import static iudx.auditing.server.querystrategy.util.Constants.RS_SUBS_WRITE_QUERY_PG;

import io.vertx.core.json.JsonObject;

public class RsSubscriptionStrategy implements AuditingServerStrategy {
  private final JsonObject config;

  public RsSubscriptionStrategy(JsonObject config) {
    this.config = config;
  }

  @Override
  public String buildPostgresWriteQuery(JsonObject request) {
    System.out.println("Request 4 subs : " + request.encode());
    String queueName = request.getString("queueName");
    String userId = request.getString("userId");
    String eventType = request.getString("eventType");
    String subscriptionType = request.getString("subscriptionType");
    String resource = request.getString("resource");

    return RS_SUBS_WRITE_QUERY_PG
        .replace("$0", RS_SUBS_TABLE_NAME)
        .replace("$1", queueName)
        .replace("$2", userId)
        .replace("$3", eventType)
        .replace("$4", subscriptionType)
        .replace("$5", resource);
  }

  @Override
  public String buildPostgresDeleteQuery(JsonObject request) {
    return null;
  }

  @Override
  public String buildImmudbWriteQuery(JsonObject request) {
    return null;
  }
}
