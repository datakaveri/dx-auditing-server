package iudx.auditing.server.postgres;

import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.pgclient.PgPool;
import io.vertx.sqlclient.Row;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;
import java.util.stream.Collector;
import java.util.stream.Collectors;

public class PostgresServiceImpl implements PostgresService {
  private static final Logger LOGGER = LogManager.getLogger(PostgresServiceImpl.class);

  private final PgPool client;

  public PostgresServiceImpl(final PgPool pgclient) {
    this.client = pgclient;
  }

  @Override
  public Future<JsonObject> executeWriteQuery(String query) {
    Promise<JsonObject> promise = Promise.promise();
    Collector<Row, ?, List<JsonObject>> rowCollector =
        Collectors.mapping(row -> row.toJson(), Collectors.toList());

    client
        .withConnection(
            connection ->
                connection.query(query).collecting(rowCollector).execute().map(row -> row.value()))
        .onSuccess(
            successHandler -> {
              JsonArray response = new JsonArray(successHandler);
              promise.complete();
            })
        .onFailure(
            failureHandler -> {
              LOGGER.error(failureHandler);
              JsonObject response = new JsonObject();
              response.put("error", "DB Error.");
              promise.fail(response.toString());
            });
    return promise.future();
  }

  @Override
  public Future<JsonObject> executeDeleteQuery(String query) {
    return null;
  }
}
