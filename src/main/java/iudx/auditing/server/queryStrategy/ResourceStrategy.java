package iudx.auditing.server.queryStrategy;

import io.vertx.core.json.JsonObject;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import static iudx.auditing.server.queryStrategy.util.Constants.*;

public class ResourceStrategy implements ServerStrategy{

  private static final Logger LOGGER = LogManager.getLogger(ResourceStrategy.class);

  @Override
  public String buildWriteQuery(JsonObject request) {


    String primaryKey = request.getString(PRIMARY_KEY);
    String api = request.getString(API);
    String userId = request.getString(USER_ID);
    long time = request.getLong(TIME);
    String resourceId = request.getString(ID);
    String isoTime =request.getString(ISO_TIME);
    String providerID =request.getString(PROVIDER_ID);
    long response_size = request.getLong(RESPONSE_SIZE);
    String databaseTableName = request.getString(DATABASE_TABLE_NAME);

    StringBuilder insertQuery =
            new StringBuilder(
                    WRITE_QUERY4RS
                            .replace("$0", databaseTableName)
                            .replace("$1", primaryKey)
                            .replace("$2", api)
                            .replace("$3", userId)
                            .replace("$4", Long.toString(time))
                            .replace("$5", resourceId)
                            .replace("$6", isoTime)
                            .replace("$7", providerID)
                            .replace("$8", Long.toString(response_size)));

    LOGGER.debug("Info: Query " + insertQuery);
    return insertQuery.toString();

  }

  @Override
  public String buildDeleteQuery(JsonObject request) {
    String primaryKey = request.getString(PRIMARY_KEY);
    String databaseTableName = request.getString(DATABASE_TABLE_NAME);
    StringBuilder deleteQuery =
            new StringBuilder(
                    DELETE_QUERY
                            .replace("$0", databaseTableName)
                            .replace("$1", primaryKey));

    LOGGER.debug("Info: Query " + deleteQuery);
    return deleteQuery.toString();
  }
}
