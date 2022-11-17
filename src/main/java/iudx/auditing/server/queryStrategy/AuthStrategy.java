package iudx.auditing.server.queryStrategy;

import io.vertx.core.json.JsonObject;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;



import static iudx.auditing.server.queryStrategy.util.Constants.*;

public class AuthStrategy implements ServerStrategy {
    private static final Logger LOGGER = LogManager.getLogger(AuthStrategy.class);

    @Override
    public String buildWriteQuery(JsonObject request) {

        String primaryKey = request.getString(PRIMARY_KEY) ;
        String body = request.getJsonObject(BODY).toString();
        String endPoint = request.getString(API);
        String methodName = request.getString(METHOD);
        long time = request.getLong(TIME);
        String userId = request.getString(USER_ID);
        String databaseTableName = request.getString(DATABASE_TABLE_NAME);

        StringBuilder query =
                new StringBuilder(
                        WRITE_QUERY4AUTH
                                .replace("$0", databaseTableName)
                                .replace("$1", primaryKey)
                                .replace("$2", body)
                                .replace("$3", endPoint)
                                .replace("$4", methodName)
                                .replace("$5", Long.toString(time))
                                .replace("$6", userId));

        LOGGER.info("Info: Query " + query);
        return query.toString();
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
