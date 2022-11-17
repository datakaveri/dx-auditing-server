package iudx.auditing.server.queryStrategy;

import io.vertx.core.json.JsonObject;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;



import static iudx.auditing.server.queryStrategy.util.Constants.*;


public class CatalogueStrategy implements ServerStrategy {
    private static final Logger LOGGER = LogManager.getLogger(CatalogueStrategy.class);

    @Override
    public String buildWriteQuery(JsonObject request) {

        String primaryKey = request.getString(PRIMARY_KEY);
        String userRole = request.getString(USER_ROLE);
        String userId = request.getString(USER_ID);
        String iid = request.getString(IID);
        String api = request.getString(API);
        String method = request.getString(HTTP_METHOD);
        String iudxID = request.getString(IUDX_ID);
        String databaseTableName = request.getString(DATABASE_TABLE_NAME);
        long time = request.getLong(TIME);

        StringBuilder insertQuery =
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
