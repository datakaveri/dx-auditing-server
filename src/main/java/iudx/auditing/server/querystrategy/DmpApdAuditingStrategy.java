package iudx.auditing.server.querystrategy;

import static iudx.auditing.server.querystrategy.util.Constants.*;

import io.vertx.core.json.JsonObject;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class DmpApdAuditingStrategy implements AuditingServerStrategy {
  private static final Logger LOGGER = LogManager.getLogger(DmpApdAuditingStrategy.class);

  private JsonObject config;

  public DmpApdAuditingStrategy(JsonObject config) {
    this.config = config;
  }

  @Override
  public String buildPostgresWriteQuery(JsonObject request) {
    LOGGER.debug("inside buildPostgresWriteQuery");
    String primaryKey = request.getString(PRIMARY_KEY);
    String userId = request.getString(USER_ID);
    String api = request.getString(API);
    String method = request.getString(HTTP_METHOD);
    JsonObject information = request.getJsonObject(INFORMATION);
    String isoTime = request.getString(ISO_TIME);
    String databaseTableName = config.getString(DMP_APD_PG_TABLE_NAME);

    ZonedDateTime zonedDateTime = ZonedDateTime.parse(isoTime);
    zonedDateTime = zonedDateTime.withZoneSameInstant(ZoneId.of("UTC"));
    LocalDateTime utcTime = zonedDateTime.toLocalDateTime();

    String query = DMP_APD_WRITE_QUERY_POSTGRES
        .replace("$0", databaseTableName)
        .replace("$1", primaryKey)
        .replace("$2", userId)
        .replace("$3", api)
        .replace("$4", method)
        .replace("$5", information.encode())
        .replace("$6", utcTime.toString());
    LOGGER.debug("Query to insert : {}", query);
    return query;
  }

  @Override
  public String buildPostgresDeleteQuery(JsonObject request) {
    LOGGER.info("inside buildPostgresDeleteQuery");
    String databaseTableName = config.getString(DMP_APD_PG_TABLE_NAME);
    String primaryKey = request.getString(PRIMARY_KEY);
    String query =  DELETE_QUERY_FOR_DMP.replace("$0", databaseTableName).replace("$1", primaryKey);
    LOGGER.debug("Query to delete : {}", query);
    return query;
  }

  @Override
  public String buildImmudbWriteQuery(JsonObject request) {
    LOGGER.debug("inside buildImmudbWriteQuery");
    String primaryKey = request.getString(PRIMARY_KEY);
    String userId = request.getString(USER_ID);
    String api = request.getString(API);
    String method = request.getString(HTTP_METHOD);
    String info = request.getString(INFORMATION);

    long epochTime = request.getLong(EPOCH_TIME);
    String isoTime = request.getString(ISO_TIME);
    LOGGER.debug("table name is : {}", config.getString(DMP_APD_IMMUDB_TABLE_NAME));
    String databaseTableName = config.getString(DMP_APD_IMMUDB_TABLE_NAME);

    String query = DMP_APD_WRITE_QUERY_IMMUDB
        .replace("$0", databaseTableName)
        .replace("$1", primaryKey)
        .replace("$2", userId)
        .replace("$3", api)
        .replace("$4", method)
        .replace("$5", info)
        .replace("$6", Long.toString(epochTime))
        .replace("$7", isoTime);
    LOGGER.debug("Immudb query : {}", query);
    return query;
  }
}
