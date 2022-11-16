package iudx.auditing.server.postgres;

import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.json.JsonObject;
import io.vertx.pgclient.PgPool;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import static iudx.auditing.server.common.Constants.INSERT_QUERY_KEY;
import static iudx.auditing.server.common.Constants.DELETE_QUERY_KEY;

public class PostgresServiceImpl implements PostgresService {
  private static final Logger LOGGER = LogManager.getLogger(PostgresServiceImpl.class);

  private final PgPool pgPool;

  public PostgresServiceImpl(final PgPool pgclient) {
    this.pgPool = pgclient;
  }

  @Override
  public Future<JsonObject> executeWriteQuery(JsonObject query) {
    Promise<JsonObject> promise = Promise.promise();
    JsonObject response = new JsonObject();
    pgPool
        .withConnection(
            connection ->
                connection.query(query.getString(INSERT_QUERY_KEY)).execute()).onComplete(
              rows->{
                if (rows.succeeded()){
                  LOGGER.info("Table Updated successfully");

                  response.put("message", "Table Updated Successfully");
                  promise.complete(response);
                }
                if(rows.failed()){
                LOGGER.error("Info failed:"+ rows.cause());
                response.put("message", rows.cause().getMessage());
                promise.fail(rows.cause().getMessage());
                }
              }
            );
    return promise.future();
  }

  @Override
  public Future<JsonObject> executeDeleteQuery(JsonObject query) {
    Promise<JsonObject> promise = Promise.promise();
    JsonObject response = new JsonObject();
    pgPool
        .withConnection(
                connection ->
                        connection.query(query.getString(DELETE_QUERY_KEY)).execute()).onComplete(
                rows->{
                  if (rows.succeeded()){
                    LOGGER.info("Table row deleted successfully");

                    response.put("message", "Table row deleted Successfully");
                    promise.complete(response);
                  }
                  if(rows.failed()){
                    LOGGER.error("Info failed:"+ rows.cause());
                    response.put("message", rows.cause().getMessage());
                    promise.fail(rows.cause().getMessage());
                  }
                }
            );

    return promise.future();
  }
}
