package iudx.auditing.server.querystrategy;

import static iudx.auditing.server.querystrategy.util.Constants.*;

import io.vertx.core.json.JsonObject;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;

public class ConsentLogAuditingStrategy implements AuditingServerStrategy {
  private final JsonObject config;

  public ConsentLogAuditingStrategy(JsonObject config) {
    this.config = config;
  }

  @Override
  public String buildPostgresWriteQuery(JsonObject request) {
    String databaseTableName = config.getString(CONSENT_LOG_PG_TABLE_NAME);
    String primaryKey = request.getString(PRIMARY_KEY);
    String itemId = request.getString(ITEM_ID);
    String eventType = request.getString(EVENT_TYPE);
    String aiuId = request.getString(AIU_ID);
    String aipId = request.getString(AIP_ID);
    String dpId = request.getString(DP_ID);
    String artifactid = request.getString(ARTIFACT_ID);

    String isoTime = request.getString(ISO_TIME);
    String itemType = request.getString(ITEM_TYPE);

    ZonedDateTime zonedDateTime = ZonedDateTime.parse(isoTime);
    zonedDateTime = zonedDateTime.withZoneSameInstant(ZoneId.of("UTC"));

    LocalDateTime utcTime = zonedDateTime.toLocalDateTime();
    String logSign = request.getString(LOG_SIGN);

    return CONSENT_LOG_WRITE_QUERY_PG
        .replace("$0", databaseTableName)
        .replace("$1", primaryKey)
        .replace("$2", itemId)
        .replace("$3", itemType)
        .replace("$4", eventType)
        .replace("$5", aiuId)
        .replace("$6", aipId)
        .replace("$7", dpId)
        .replace("$8", artifactid)
        .replace("$9", utcTime.toString())
        .replace("$a", logSign);
  }

  @Override
  public String buildPostgresDeleteQuery(JsonObject request) {
    String databaseTableName = config.getString(CONSENT_LOG_PG_TABLE_NAME);
    String primaryKey = request.getString(PRIMARY_KEY);

    return DELETE_QUERY.replace("$0", databaseTableName).replace("$1", primaryKey);
  }

  @Override
  public String buildImmudbWriteQuery(JsonObject request) {
    String databaseTableName = config.getString(CONSENT_LOG_IMMUDB_TABLE_NAME);
    String primaryKey = request.getString(PRIMARY_KEY);
    String itemId = request.getString(ITEM_ID);
    String eventType = request.getString(EVENT_TYPE);
    String aiuId = request.getString(AIU_ID);
    String aipId = request.getString(AIP_ID);
    String dpId = request.getString(DP_ID);
    String artifactid = request.getString(ARTIFACT_ID);
    String isoTime = request.getString(ISO_TIME);
    String itemType = request.getString(ITEM_TYPE);

    /*ZonedDateTime zonedDateTime = ZonedDateTime.parse(isoTime);
    zonedDateTime = zonedDateTime.withZoneSameInstant(ZoneId.of("UTC"));

    LocalDateTime utcTime = zonedDateTime.toLocalDateTime();*/
    String logSign = request.getString(LOG_SIGN);
    // TODO: Decide query whether we need to store log in immudb as we can't store big string in
    // immudb
    return CONSENT_LOG_WRITE_QUERY_IMMUDB
        .replace("$0", databaseTableName)
        .replace("$1", primaryKey)
        .replace("$2", itemId)
        .replace("$3", itemType)
        .replace("$4", eventType)
        .replace("$5", aiuId)
        .replace("$6", aipId)
        .replace("$7", dpId)
        .replace("$8", artifactid)
        .replace("$9", isoTime)
        .replace("$a", logSign);
  }
}
