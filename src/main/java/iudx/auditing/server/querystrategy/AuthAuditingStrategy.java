package iudx.auditing.server.querystrategy;

import static iudx.auditing.server.querystrategy.util.Constants.API;
import static iudx.auditing.server.querystrategy.util.Constants.AUTH_IMMUDB_TABLE_NAME;
import static iudx.auditing.server.querystrategy.util.Constants.AUTH_PG_TABLE_NAME;
import static iudx.auditing.server.querystrategy.util.Constants.AUTH_WRITE_QUERY_IMMUDB;
import static iudx.auditing.server.querystrategy.util.Constants.AUTH_WRITE_QUERY_PG;
import static iudx.auditing.server.querystrategy.util.Constants.BODY;
import static iudx.auditing.server.querystrategy.util.Constants.DELETE_QUERY;
import static iudx.auditing.server.querystrategy.util.Constants.EPOCH_TIME;
import static iudx.auditing.server.querystrategy.util.Constants.PRIMARY_KEY;
import static iudx.auditing.server.querystrategy.util.Constants.USER_ID;
import static javax.xml.transform.OutputKeys.METHOD;

import io.vertx.core.json.JsonObject;

public class AuthAuditingStrategy implements AuditingServerStrategy {
  JsonObject config;

  AuthAuditingStrategy(JsonObject config) {
    this.config = config;
  }

  @Override
  public String buildPostgresWriteQuery(JsonObject request) {
    String primaryKey = request.getString(PRIMARY_KEY);
    String body = request.getJsonObject(BODY).toString();
    String endPoint = request.getString(API);
    String methodName = request.getString(METHOD);
    long time = request.getLong(EPOCH_TIME);
    String userId = request.getString(USER_ID);
    String databaseTableName = config.getString(AUTH_PG_TABLE_NAME);

    return AUTH_WRITE_QUERY_PG
        .replace("$0", databaseTableName)
        .replace("$1", primaryKey)
        .replace("$2", body)
        .replace("$3", endPoint)
        .replace("$4", methodName)
        .replace("$5", Long.toString(time))
        .replace("$6", userId);
  }

  @Override
  public String buildPostgresDeleteQuery(JsonObject request) {
    String primaryKey = request.getString(PRIMARY_KEY);
    String databaseTableName = config.getString(AUTH_PG_TABLE_NAME);

    return DELETE_QUERY.replace("$0", databaseTableName).replace("$1", primaryKey);
  }

  @Override
  public String buildImmudbWriteQuery(JsonObject request) {
    String primaryKey = request.getString(PRIMARY_KEY);
    String body = request.getJsonObject(BODY).toString();
    String endPoint = request.getString(API);
    String methodName = request.getString(METHOD);
    long time = request.getLong(EPOCH_TIME);
    String userId = request.getString(USER_ID);
    String databaseTableName = config.getString(AUTH_IMMUDB_TABLE_NAME);

    return AUTH_WRITE_QUERY_IMMUDB
        .replace("$0", databaseTableName)
        .replace("$1", primaryKey)
        .replace("$2", body)
        .replace("$3", endPoint)
        .replace("$4", methodName)
        .replace("$5", Long.toString(time))
        .replace("$6", userId);
  }
}
