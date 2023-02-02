package iudx.auditing.server.queryStrategy;

import static iudx.auditing.server.queryStrategy.util.Constants.API;
import static iudx.auditing.server.queryStrategy.util.Constants.DELETE_QUERY;
import static iudx.auditing.server.queryStrategy.util.Constants.EPOCH_TIME;
import static iudx.auditing.server.queryStrategy.util.Constants.ID;
import static iudx.auditing.server.queryStrategy.util.Constants.ISO_TIME;
import static iudx.auditing.server.queryStrategy.util.Constants.PRIMARY_KEY;
import static iudx.auditing.server.queryStrategy.util.Constants.PROVIDER_ID;
import static iudx.auditing.server.queryStrategy.util.Constants.RS_IMMUDB_TABLE_NAME;
import static iudx.auditing.server.queryStrategy.util.Constants.RS_PG_TABLE_NAME;
import static iudx.auditing.server.queryStrategy.util.Constants.RS_WRITE_QUERY;
import static iudx.auditing.server.queryStrategy.util.Constants.SIZE;
import static iudx.auditing.server.queryStrategy.util.Constants.USER_ID;

import io.vertx.core.json.JsonObject;

public class ResourceAuditingStrategy implements AuditingServerStrategy {
  private final JsonObject config;

  public ResourceAuditingStrategy(JsonObject config) {
    this.config = config;
  }

  @Override
  public String buildPostgresWriteQuery(JsonObject request) {
    String userId = request.getString(USER_ID);
    String primaryKey = request.getString(PRIMARY_KEY);
    String resourceId = request.getString(ID);
    String providerID = request.getString(PROVIDER_ID);
    String api = request.getString(API);
    long time = request.getLong(EPOCH_TIME);
    String isoTime = request.getString(ISO_TIME);
    long response_size = request.getLong(SIZE);
    String databaseTableName = config.getString(RS_PG_TABLE_NAME);

    return RS_WRITE_QUERY
        .replace("$0", databaseTableName)
        .replace("$1", primaryKey)
        .replace("$2", api)
        .replace("$3", userId)
        .replace("$4", Long.toString(time))
        .replace("$5", resourceId)
        .replace("$6", isoTime)
        .replace("$7", providerID)
        .replace("$8", Long.toString(response_size));
  }

  @Override
  public String buildPostgresDeleteQuery(JsonObject request) {
    String databaseTableName = config.getString(RS_PG_TABLE_NAME);
    String primaryKey = request.getString(PRIMARY_KEY);

    return DELETE_QUERY.replace("$0", databaseTableName).replace("$1", primaryKey);
  }

  @Override
  public String buildImmudbWriteQuery(JsonObject request) {
    String userId = request.getString(USER_ID);
    String primaryKey = request.getString(PRIMARY_KEY);
    String resourceId = request.getString(ID);
    String providerID = request.getString(PROVIDER_ID);
    String api = request.getString(API);
    long time = request.getLong(EPOCH_TIME);
    String isoTime = request.getString(ISO_TIME);
    long response_size = request.getLong(SIZE);
    String databaseTableName = config.getString(RS_IMMUDB_TABLE_NAME);

    return RS_WRITE_QUERY
        .replace("$0", databaseTableName)
        .replace("$1", primaryKey)
        .replace("$2", api)
        .replace("$3", userId)
        .replace("$4", Long.toString(time))
        .replace("$5", resourceId)
        .replace("$6", isoTime)
        .replace("$7", providerID)
        .replace("$8", Long.toString(response_size));
  }
}
