package iudx.auditing.server.queryStrategy;

import io.vertx.core.json.JsonObject;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.time.ZonedDateTime;
import java.util.UUID;

import static iudx.auditing.server.queryStrategy.util.Constants.*;


public class CatalogueStrategy implements ServerStrategy {
    private static final Logger LOGGER = LogManager.getLogger(ResourceStrategy.class);

    @Override
    public String buildWriteQuery(JsonObject request) {

        String primaryKey = UUID.randomUUID().toString().replace("-", "");
        String userRole = request.getString(USER_ROLE);
        String userId = request.getString(USER_ID);
        String iid = request.getString(IID);
        String api = request.getString(API);
        String method = request.getString(HTTP_METHOD);
        String iudxID = request.getString(IUDX_ID);
        String databaseTableName = request.getString(TABLE_NAME);
        ZonedDateTime zst = ZonedDateTime.now();
        LOGGER.debug("TIME ZST: " + zst);
        long time = getEpochTime(zst);

        StringBuilder query =
                new StringBuilder(
                        WRITE_QUERY4CAT
                                .replace("$0", databaseTableName)
                                .replace("$1", primaryKey)
                                .replace("$2", userRole)
                                .replace("$3", userId)
                                .replace("$4", iid)
                                .replace("$5", api)
                                .replace("$6", method)
                                .replace("$7", Long.toString(time))
                                .replace("$8", iudxID));

        LOGGER.debug("Query " + query);
        return query.toString();
    }

    private long getEpochTime(ZonedDateTime time) {
        return time.toInstant().toEpochMilli();
    }
}
