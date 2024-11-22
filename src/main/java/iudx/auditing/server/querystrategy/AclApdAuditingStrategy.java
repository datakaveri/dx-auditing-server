package iudx.auditing.server.querystrategy;

import static iudx.auditing.server.querystrategy.util.Constants.APD_IMMUDB_TABLE_NAME;
import static iudx.auditing.server.querystrategy.util.Constants.APD_PG_TABLE_NAME;
import static iudx.auditing.server.querystrategy.util.Constants.APD_WRITE_QUERY_IMMUDB;
import static iudx.auditing.server.querystrategy.util.Constants.APD_WRITE_QUERY_PG;
import static iudx.auditing.server.querystrategy.util.Constants.API;
import static iudx.auditing.server.querystrategy.util.Constants.BODY;
import static iudx.auditing.server.querystrategy.util.Constants.DELETE_QUERY;
import static iudx.auditing.server.querystrategy.util.Constants.EPOCH_TIME;
import static iudx.auditing.server.querystrategy.util.Constants.HTTP_METHOD;
import static iudx.auditing.server.querystrategy.util.Constants.ISO_TIME;
import static iudx.auditing.server.querystrategy.util.Constants.PRIMARY_KEY;
import static iudx.auditing.server.querystrategy.util.Constants.SIZE;
import static iudx.auditing.server.querystrategy.util.Constants.USER_ID;

import io.vertx.core.json.JsonObject;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;

public class AclApdAuditingStrategy implements AuditingServerStrategy {
  private JsonObject config;

  public AclApdAuditingStrategy(JsonObject config) {
    this.config = config;
  }

  @Override
  public String buildPostgresWriteQuery(JsonObject request) {
    String primaryKey = request.getString(PRIMARY_KEY);
    String userId = request.getString(USER_ID);
    String api = request.getString(API);
    String method = request.getString(HTTP_METHOD);
    JsonObject body = request.getJsonObject(BODY);
    long responseSize = request.getLong(SIZE);
    String isoTime = request.getString(ISO_TIME);
    String databaseTableName = config.getString(APD_PG_TABLE_NAME);

    ZonedDateTime zonedDateTime = ZonedDateTime.parse(isoTime);
    zonedDateTime = zonedDateTime.withZoneSameInstant(ZoneId.of("UTC"));

    LocalDateTime utcTime = zonedDateTime.toLocalDateTime();

    return APD_WRITE_QUERY_PG
        .replace("$0", databaseTableName)
        .replace("$1", primaryKey)
        .replace("$2", userId)
        .replace("$3", api)
        .replace("$4", method)
        .replace("$5", body.encode())
        .replace("$6", Long.toString(responseSize))
        .replace("$7", utcTime.toString());
  }

  @Override
  public String buildPostgresDeleteQuery(JsonObject request) {
    String databaseTableName = config.getString(APD_PG_TABLE_NAME);
    String primaryKey = request.getString(PRIMARY_KEY);
    return DELETE_QUERY.replace("$0", databaseTableName).replace("$1", primaryKey);
  }

  @Override
  public String buildImmudbWriteQuery(JsonObject request) {
    String primaryKey = request.getString(PRIMARY_KEY);
    String userId = request.getString(USER_ID);
    String api = request.getString(API);
    String method = request.getString(HTTP_METHOD);
    JsonObject body = request.getJsonObject(BODY);

    long responseSize = request.getLong(SIZE);
    long epochTime = request.getLong(EPOCH_TIME);
    String isoTime = request.getString(ISO_TIME);
    String databaseTableName = config.getString(APD_IMMUDB_TABLE_NAME);

    return APD_WRITE_QUERY_IMMUDB
        .replace("$0", databaseTableName)
        .replace("$1", primaryKey)
        .replace("$2", userId)
        .replace("$3", api)
        .replace("$4", method)
        .replace("$5", body.encode())
        .replace("$6", Long.toString(responseSize))
        .replace("$7", Long.toString(epochTime))
        .replace("$8", isoTime);
  }
}
