package iudx.auditing.server.queryStrategy;

import io.vertx.core.json.JsonObject;

import static iudx.auditing.server.queryStrategy.util.Constants.*;

public class CatalogueAuditingStrategy implements AuditingServerStrategy {
  private final JsonObject config;

  CatalogueAuditingStrategy(JsonObject config) {
    this.config = config;
  }

  @Override
  public String buildPostgresWriteQuery(JsonObject request) {
    String primaryKey = request.getString(PRIMARY_KEY);
    String userRole = request.getString(USER_ROLE);
    String userId = request.getString(USER_ID);
    String iid = request.getString(IID);
    String api = request.getString(API);
    String method = request.getString(HTTP_METHOD);
    String iudxID = request.getString(IUDX_ID);
    String databaseTableName = config.getString(CAT_PG_TABLE_NAME);
    long time = request.getLong(EPOCH_TIME);

    return CAT_WRITE_QUERY_PG
        .replace("$0", databaseTableName)
        .replace("$1", primaryKey)
        .replace("$2", userRole)
        .replace("$3", userId)
        .replace("$4", iid)
        .replace("$5", api)
        .replace("$6", method)
        .replace("$7", Long.toString(time))
        .replace("$8", iudxID);
  }

  @Override
  public String buildPostgresDeleteQuery(JsonObject request) {
    String primaryKey = request.getString(PRIMARY_KEY);
    String databaseTableName = config.getString(CAT_PG_TABLE_NAME);

    return DELETE_QUERY.replace("$0", databaseTableName).replace("$1", primaryKey);
  }

  @Override
  public String buildImmudbWriteQuery(JsonObject request) {
    String primaryKey = request.getString(PRIMARY_KEY);
    String userRole = request.getString(USER_ROLE);
    String userId = request.getString(USER_ID);
    String iid = request.getString(IID);
    String api = request.getString(API);
    String method = request.getString(HTTP_METHOD);
    String iudxID = request.getString(IUDX_ID);
    String databaseTableName = config.getString(CAT_IMMUDB_TABLE_NAME);
    long time = request.getLong(EPOCH_TIME);

    return CAT_WRITE_QUERY_IMMUDB
        .replace("$0", databaseTableName)
        .replace("$1", primaryKey)
        .replace("$2", userRole)
        .replace("$3", userId)
        .replace("$4", iid)
        .replace("$5", api)
        .replace("$6", method)
        .replace("$7", Long.toString(time))
        .replace("$8", iudxID);
  }
}
