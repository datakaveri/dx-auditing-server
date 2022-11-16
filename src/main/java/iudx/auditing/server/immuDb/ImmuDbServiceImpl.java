package iudx.auditing.server.immuDb;

import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.json.JsonObject;
import io.vertx.pgclient.PgPool;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ImmuDbServiceImpl implements ImmuDbService {
    private static final Logger LOGGER = LogManager.getLogger(ImmuDbServiceImpl.class);
    private final PgPool pgClient;

    public ImmuDbServiceImpl(PgPool pgClient) {
        this.pgClient = pgClient;
    }


    @Override
    public Future<JsonObject> executeWriteQuery(String query) {
        Promise<JsonObject> promise = Promise.promise();
        JsonObject response = new JsonObject();
        pgClient.withConnection(connection -> connection.query(query).execute())
                .onComplete(
                        rows -> {
                            if (rows.succeeded()) {
                                LOGGER.debug("Table updated successfully");

                                response.put("message", "Table Updated Successfully");
                                promise.complete(response);
                            } else {
                                LOGGER.error("Info: failed :" + rows.cause());
                                response.put("message", rows.cause().getMessage());
                                promise.fail(rows.cause().getMessage());
                            }
                        });
        return promise.future();
    }
}
