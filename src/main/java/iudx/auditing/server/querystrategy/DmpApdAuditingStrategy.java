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
    LOGGER.debug("request : {}", request.encodePrettily() );
    String primaryKey = request.getString(PRIMARY_KEY);
    String userId = request.getString(USER_ID);
    String api = request.getString(API);
    String method = request.getString(HTTP_METHOD);
    String information = request.getString(INFORMATION);
    String isoTime = request.getString(ISO_TIME);
    String databaseTableName = config.getString(DMP_APD_PG_TABLE_NAME);

    ZonedDateTime zonedDateTime = ZonedDateTime.parse(isoTime);
    zonedDateTime = zonedDateTime.withZoneSameInstant(ZoneId.of("UTC"));
    LocalDateTime utcTime = zonedDateTime.toLocalDateTime();

    return DMP_APD_WRITE_QUERY_POSTGRES
        .replace("$0", databaseTableName)
        .replace("$1", primaryKey)
        .replace("$2", userId)
        .replace("$3", api)
        .replace("$4", method)
        .replace("$5", information)
        .replace("$6", utcTime.toString());
  }

  @Override
  public String buildPostgresDeleteQuery(JsonObject request) {
    LOGGER.info("inside buildPostgresDeleteQuery");
    LOGGER.debug("request : {}", request.encodePrettily() );
    String databaseTableName = config.getString(DMP_APD_PG_TABLE_NAME);
    String primaryKey = request.getString(PRIMARY_KEY);
    return DELETE_QUERY.replace("$0", databaseTableName).replace("$1", primaryKey);
  }

  @Override
  public String buildImmudbWriteQuery(JsonObject request) {
    LOGGER.debug("inside buildImmudbWriteQuery");
    LOGGER.debug("request : {}", request.encodePrettily() );
    String primaryKey = request.getString(PRIMARY_KEY);
    String userId = request.getString(USER_ID);
    String api = request.getString(API);
    String method = request.getString(HTTP_METHOD);
    String info = request.getString(INFORMATION);

    long epochTime = request.getLong(EPOCH_TIME);
    String isoTime = request.getString(ISO_TIME);
    String databaseTableName = config.getString(DMP_APD_IMMUDB_TABLE_NAME);

    return DMP_APD_WRITE_QUERY_IMMUDB
        .replace("$0", databaseTableName)
        .replace("$1", primaryKey)
        .replace("$2", userId)
        .replace("$3", api)
        .replace("$4", method)
        .replace("$5", info)
        .replace("$6", Long.toString(epochTime))
        .replace("$7", isoTime);
  }
}
