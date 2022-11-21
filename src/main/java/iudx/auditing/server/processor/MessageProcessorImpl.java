package iudx.auditing.server.processor;

import static iudx.auditing.server.common.Constants.IMMUDB_WRITE_QUERY;
import static iudx.auditing.server.common.Constants.ORIGIN;
import static iudx.auditing.server.common.Constants.PG_DELETE_QUERY_KEY;
import static iudx.auditing.server.common.Constants.PG_INSERT_QUERY_KEY;

import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import iudx.auditing.server.immudb.ImmudbService;
import iudx.auditing.server.postgres.PostgresService;
import iudx.auditing.server.queryStrategy.ServerOrigin;
import iudx.auditing.server.queryStrategy.ServerOriginContextFactory;
import iudx.auditing.server.queryStrategy.ServerStrategy;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class MessageProcessorImpl implements MessageProcessService {

  private static final Logger LOGGER = LogManager.getLogger(MessageProcessorImpl.class);
  private final Vertx vertx;
  private final PostgresService postgresService;
  private final ImmudbService immudbService;
  private final JsonObject config;

  public MessageProcessorImpl(
      Vertx vertx,
      PostgresService postgresService,
      ImmudbService immudbService,
      JsonObject config) {
    this.vertx = vertx;
    this.postgresService = postgresService;
    this.immudbService = immudbService;
    this.config = config;
  }

  @Override
  public Future<JsonObject> process(JsonObject message) {
    LOGGER.info("message processing starts : ");
    JsonObject queries = queryBuilder(message);
    Promise<JsonObject> promise = Promise.promise();
    Future<JsonObject> insertInPostgres = postgresService.executeWriteQuery(queries);
    insertInPostgres
        .onSuccess(
            insertInImmudbHandler -> {
              Future<JsonObject> insertInImmudb = immudbService.executeWriteQuery(queries);
              insertInImmudb.onComplete(
                  immudbHandler -> {
                    if (insertInImmudb.succeeded()) {
                      promise.complete(message);
                    } else {
                      Future<JsonObject> deleteFromPostgres =
                          postgresService.executeDeleteQuery(queries);
                      deleteFromPostgres.onSuccess(
                          postgresHandler -> {
                            if (deleteFromPostgres.succeeded()) {
                              promise.fail(immudbHandler.cause().getMessage());
                              LOGGER.info("success delete");
                            }
                          });
                    }
                  });
            })
        .onFailure(
            failureHandler -> {
              promise.fail("failed to insert in postgres " + failureHandler.getCause());
            });

    return promise.future();
  }

  private JsonObject queryBuilder(JsonObject request) {
    String origin = request.getString(ORIGIN);
    ServerOrigin serverOrigin = ServerOrigin.fromRole(origin);
    ServerOriginContextFactory serverOriginContextFactory = new ServerOriginContextFactory(config);
    ServerStrategy serverStrategy = serverOriginContextFactory.create(serverOrigin);
    String postgresWriteQuery = serverStrategy.buildPostgresWriteQuery(request);
    String postgresDeleteQuery = serverStrategy.buildPostgresDeleteQuery(request);
    String immudbWriteQuery = serverStrategy.buildImmudbDeleteQuery(request);
    return new JsonObject()
        .put(PG_INSERT_QUERY_KEY, postgresWriteQuery)
        .put(PG_DELETE_QUERY_KEY, postgresDeleteQuery)
        .put(IMMUDB_WRITE_QUERY, immudbWriteQuery);
  }
}
