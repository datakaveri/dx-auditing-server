package iudx.auditing.server.processor;

import static iudx.auditing.server.common.Constants.DELIVERY_TAG;
import static iudx.auditing.server.common.Constants.IMMUDB_WRITE_QUERY;
import static iudx.auditing.server.common.Constants.ORIGIN;
import static iudx.auditing.server.common.Constants.PG_DELETE_QUERY_KEY;
import static iudx.auditing.server.common.Constants.PG_INSERT_QUERY_KEY;

import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.json.JsonObject;
import iudx.auditing.server.cache.CacheService;
import iudx.auditing.server.immudb.ImmudbService;
import iudx.auditing.server.postgres.PostgresService;
import iudx.auditing.server.processor.subscription.SubscriptionAuditService;
import iudx.auditing.server.processor.subscription.SubscriptionAuditServiceImpl;
import iudx.auditing.server.querystrategy.AuditingServerStrategy;
import iudx.auditing.server.querystrategy.ServerOrigin;
import iudx.auditing.server.querystrategy.ServerOriginContextFactory;
import iudx.auditing.server.rabbitmq.RabbitMqService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class MessageProcessorImpl implements MessageProcessService {

  private static final Logger LOGGER = LogManager.getLogger(MessageProcessorImpl.class);
  private final PostgresService postgresService;
  private final ImmudbService immudbService;
  private final JsonObject config;
  CacheService cacheService;
  private SubscriptionAuditService subsAuditService;

  public MessageProcessorImpl(PostgresService postgresService, ImmudbService immudbService,
                              RabbitMqService rabbitMqService, CacheService cacheService,
                              JsonObject config) {
    this.postgresService = postgresService;
    this.immudbService = immudbService;
    this.cacheService = cacheService;
    this.config = config;
    this.subsAuditService = new SubscriptionAuditServiceImpl(rabbitMqService, cacheService);
  }

  @Override
  public Future<JsonObject> processAuditEventMessages(JsonObject message) {
    LOGGER.info("message processing starts : ");
    JsonObject queries = queryBuilder(message);
    queries.put(DELIVERY_TAG, message.getLong(DELIVERY_TAG));
    queries.put(ORIGIN, message.getString(ORIGIN));
    Promise<JsonObject> promise = Promise.promise();
    databaseOperations(queries)
        .onComplete(dbHandler -> {
          if (dbHandler.succeeded()) {
            promise.complete(dbHandler.result());
          } else {
            LOGGER.error(dbHandler.cause());
            promise.fail(dbHandler.cause());
          }
        });
    return promise.future();
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

  @Override
  public Future<Void> processSubscriptionMonitoringMessages(JsonObject message) {
    Promise<Void> promise = Promise.promise();
    subsAuditService.generateAuditLog(message.getString("id"), message, cacheService).onComplete(
        logHandler -> {
          if (logHandler.succeeded()) {
            promise.complete();
          } else {
            promise.fail(logHandler.cause());
          }

        });
    return promise.future();
  }

  private Future<JsonObject> databaseOperations(JsonObject queries) {
    Promise<JsonObject> promise = Promise.promise();
    Future<JsonObject> insertInPostgres = postgresService.executeWriteQuery(queries);
    insertInPostgres.onSuccess(insertInImmudbHandler -> {
      Future<JsonObject> insertInImmudb = immudbService.executeWriteQuery(queries);
      insertInImmudb.onComplete(immudbHandler -> {
        if (insertInImmudb.succeeded()) {
          promise.complete(queries);
        } else {
          Future<JsonObject> deleteFromPostgres = postgresService.executeDeleteQuery(queries);
          deleteFromPostgres.onComplete(postgresHandler -> {
            if (deleteFromPostgres.succeeded()) {
              LOGGER
                  .error("Rollback : success delete. Message Origin: {}",
                      queries.getString(ORIGIN));
              promise.fail(immudbHandler.cause().getMessage());
            } else {
              LOGGER.info("Rollback : delete failed");
              promise.fail(deleteFromPostgres.cause().getMessage());
            }
          });
        }
      });
    }).onFailure(failureHandler -> {
      promise.fail("failed to insert in postgres " + failureHandler.getCause());
    });

    return promise.future();
  }
}
