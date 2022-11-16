package iudx.auditing.server.queryStrategy;

import io.vertx.core.json.JsonObject;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

import static iudx.auditing.server.queryStrategy.util.Constants.*;


public class ResourceStrategy implements ServerStrategy{
  @Override
  public String buildWriteQuery(JsonObject request) {

    String userId = request.getString(USER_ID);
    String primaryKey = UUID.randomUUID().toString().replace("-", "");
    String resourceId = request.getString(ID);
    String providerID =
            resourceId.substring(0, resourceId.indexOf('/', resourceId.indexOf('/') + 1));
    String api = request.getString(API);
    ZonedDateTime zst = ZonedDateTime.now(ZoneId.of("Asia/Kolkata"));
    long time = getEpochTime(zst);
    String isoTime = zst.truncatedTo(ChronoUnit.SECONDS).toString();
    long response_size = request.getLong(SIZE);
    String databaseTableName = request.getString(TABLE_NAME);

    StringBuilder insertQuery =
            new StringBuilder(
                    WRITE_QUERY
                            .replace("$0", databaseTableName)
                            .replace("$1", primaryKey)
                            .replace("$2", api)
                            .replace("$3", userId)
                            .replace("$4", Long.toString(time))
                            .replace("$5", resourceId)
                            .replace("$6", isoTime)
                            .replace("$7", providerID)
                            .replace("$8", Long.toString(response_size)));

    return insertQuery.toString();
  }

  @Override
  public String buildDeleteQuery(JsonObject request) {
    String databaseTableName = request.getString(TABLE_NAME);
    String resourceId = request.getString(ID);
    String providerID =
            resourceId.substring(0, resourceId.indexOf('/', resourceId.indexOf('/') + 1));

    StringBuilder deleteQuery = new StringBuilder(
            DELETE_QUERY
                    .replace("$0", databaseTableName)
                    .replace("$1", providerID));
    return deleteQuery.toString();
  }

  private long getEpochTime(ZonedDateTime time) {
    return time.toInstant().toEpochMilli();
  }
}
