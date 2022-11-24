package iudx.auditing.server.immudb;

import static iudx.auditing.server.common.Constants.IMMUDB_WRITE_QUERY;
import static iudx.auditing.server.common.Constants.RESULT;

import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.json.JsonObject;
import io.vertx.pgclient.PgPool;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ImmudbServiceImpl implements ImmudbService {
  private static final Logger LOGGER = LogManager.getLogger(ImmudbServiceImpl.class);
  private final PgPool pgClient;

  public ImmudbServiceImpl(PgPool pgClient) {
    this.pgClient = pgClient;
  }

  @Override
  public Future<JsonObject> executeWriteQuery(JsonObject query) {
    Promise<JsonObject> promise = Promise.promise();
    JsonObject response = new JsonObject();
    pgClient
        .withConnection(
            connection -> connection.query(query.getString(IMMUDB_WRITE_QUERY)).execute())
        .onComplete(
            rows -> {
              if (rows.succeeded()) {
                LOGGER.debug("Immudb Table updated successfully");
                response.put(RESULT, "Table Updated Successfully");
                promise.complete(response);
              } else {
                LOGGER.error("Info: failed :" + rows.cause());
                response.put(RESULT, rows.cause());
                promise.fail(rows.cause());
              }
            });
    return promise.future();
  }
}
