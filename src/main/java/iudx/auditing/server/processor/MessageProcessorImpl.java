package iudx.auditing.server.processor;

import static iudx.auditing.server.common.Constants.*;
import static iudx.auditing.server.querystrategy.ServerOrigin.RS_SERVER_SUBS;

import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.json.JsonObject;
import iudx.auditing.server.immudb.ImmudbService;
import iudx.auditing.server.postgres.PostgresService;
import iudx.auditing.server.querystrategy.AuditingServerStrategy;
import iudx.auditing.server.querystrategy.ServerOrigin;
import iudx.auditing.server.querystrategy.ServerOriginContextFactory;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class MessageProcessorImpl implements MessageProcessService {

  private static final Logger LOGGER = LogManager.getLogger(MessageProcessorImpl.class);
  private final PostgresService postgresService;
  private final ImmudbService immudbService;
  private final JsonObject config;

  public MessageProcessorImpl(
      PostgresService postgresService, ImmudbService immudbService, JsonObject config) {
    this.postgresService = postgresService;
    this.immudbService = immudbService;
    this.config = config;
  }

  @Override
  public Future<JsonObject> process(JsonObject message) {
    LOGGER.info("message processing starts : ");
    JsonObject queries = queryBuilder(message);
    Promise<JsonObject> promise = Promise.promise();
    if (!message.getString(ORIGIN).equals(RS_SERVER_SUBS.getOriginRole())) {
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
                        deleteFromPostgres.onComplete(
                            postgresHandler -> {
                              if (deleteFromPostgres.succeeded()) {
                                LOGGER.error(
                                    "Rollback : success delete. Message Origin: {}",
                                    message.getString(ORIGIN));
                                promise.fail(immudbHandler.cause().getMessage());
                              } else {
                                LOGGER.info("Rollback : delete failed");
                                promise.fail(deleteFromPostgres.cause().getMessage());
                              }
                            });
                      }
                    });
              })
          .onFailure(
              failureHandler -> {
                promise.fail("failed to insert in postgres " + failureHandler.getCause());
              });
    } else {
      process4AuditingSubscription(queries, promise);
    }

    return promise.future();
  }

  private void process4AuditingSubscription(JsonObject queries, Promise<JsonObject> promise) {
    LOGGER.debug("inside process4AuditingSubscription");
    Future<JsonObject> insertInPostgres = postgresService.executeWriteQuery(queries);
    insertInPostgres.onComplete(
        handler -> {
          if (handler.succeeded()) {
            promise.tryComplete(handler.result());
          } else {
            promise.tryFail(handler.cause().getMessage());
          }
        });
    Future<JsonObject> deleteFromPostgres = postgresService.executeDeleteQuery(queries);
    deleteFromPostgres.onComplete(
        handler -> {
          if (handler.succeeded()) {
            promise.tryComplete(handler.result());
          } else {
            promise.tryFail(handler.cause().getMessage());
          }
        });
  }

  private JsonObject queryBuilder(JsonObject request) {
    String origin = request.getString(ORIGIN);
    ServerOrigin serverOrigin = ServerOrigin.fromRole(origin);
    ServerOriginContextFactory serverOriginContextFactory = new ServerOriginContextFactory(config);
    AuditingServerStrategy serverStrategy = serverOriginContextFactory.create(serverOrigin);
    String postgresWriteQuery = serverStrategy.buildPostgresWriteQuery(request);
    String postgresDeleteQuery = serverStrategy.buildPostgresDeleteQuery(request);
    String immudbWriteQuery = serverStrategy.buildImmudbWriteQuery(request);
    return new JsonObject()
        .put(PG_INSERT_QUERY_KEY, postgresWriteQuery)
        .put(PG_DELETE_QUERY_KEY, postgresDeleteQuery)
        .put(IMMUDB_WRITE_QUERY, immudbWriteQuery)
        .put(ORIGIN, origin);
  }
}
