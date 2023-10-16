package iudx.auditing.server.processor;

import static iudx.auditing.server.common.Constants.DELIVERY_TAG;
import static iudx.auditing.server.common.Constants.IMMUDB_WRITE_QUERY;
import static iudx.auditing.server.common.Constants.ORIGIN;
import static iudx.auditing.server.common.Constants.PG_DELETE_QUERY_KEY;
import static iudx.auditing.server.common.Constants.PG_INSERT_QUERY_KEY;
import static iudx.auditing.server.querystrategy.util.Constants.DELEGATOR_ID;
import static iudx.auditing.server.querystrategy.util.Constants.PROVIDER_ID;
import static iudx.auditing.server.querystrategy.util.Constants.RESOURCE_GROUP;
import static iudx.auditing.server.querystrategy.util.Constants.TYPE;

import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.json.JsonObject;
import iudx.auditing.server.immudb.ImmudbService;
import iudx.auditing.server.postgres.PostgresService;
import iudx.auditing.server.processor.subscription.SubscriptionAuditService;
import iudx.auditing.server.processor.subscription.SubscriptionAuditServiceImpl;
import iudx.auditing.server.processor.subscription.SubscriptionUser;
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
  private final RabbitMqService rabbitMqService;
  private final JsonObject config;
  private SubscriptionAuditService subsAuditService;

  public MessageProcessorImpl(PostgresService postgresService, ImmudbService immudbService,
                              RabbitMqService rabbitMqService, JsonObject config) {
    this.postgresService = postgresService;
    this.immudbService = immudbService;
    this.rabbitMqService = rabbitMqService;
    this.config = config;
    this.subsAuditService = new SubscriptionAuditServiceImpl(rabbitMqService);
  }

  @Override
  public Future<JsonObject> processAuditEventMessages(JsonObject message) {
    LOGGER.info("message processing starts : ");
    JsonObject queries = queryBuilder(message);
    queries.put(DELIVERY_TAG, message.getLong(DELIVERY_TAG));
    queries.put(ORIGIN, message.getString(ORIGIN));
    Promise<JsonObject> promise = Promise.promise();
    if (!message.containsKey("eventType")) {
      databaseOperations(queries)
          .onComplete(dbHandler -> {
            if (dbHandler.succeeded()) {
              LOGGER.info(dbHandler.result());
              promise.complete(dbHandler.result());
            } else {
              LOGGER.error(dbHandler.cause());
              promise.fail(dbHandler.cause());
            }
          });
    } else {
      String eventType = message.getString("eventType");
      if (eventType == null || eventType.isEmpty()) {
        promise.tryFail("EventType is null or empty for subscription related processing");
      } else {
        process4AuditingSubscription(queries, message, promise, eventType);
      }
    }

    return promise.future();
  }

  private void process4AuditingSubscription(JsonObject queries, JsonObject message,
                                            Promise<JsonObject> promise, String eventType) {
    LOGGER.debug("inside process4AuditingSubscription");

    if ("SUBS_CREATED".equals(eventType)) {
      SubscriptionUser subsConsumer = new SubscriptionUser(message.getString("userid"),
          message.getString("subscriptionID"), message.getString("id"),
          message.getString(PROVIDER_ID), message.getString(RESOURCE_GROUP),
          message.getString(DELEGATOR_ID), message.getString(TYPE));
      subsAuditService.addSubsConsumer(subsConsumer);
      databaseOperations(queries)
          .onComplete(dbHandler -> {
            if (dbHandler.succeeded()) {
              promise.complete(dbHandler.result());
            } else {
              LOGGER.error(dbHandler.cause());
            }
          });
    } else if ("SUBS_DELETED".equals(eventType)) {
      subsAuditService.deleteSubsConsumer(message.getString("subscriptionID"));
      databaseOperations(queries)
          .onComplete(dbHandler -> {
            if (dbHandler.succeeded()) {
              promise.complete(dbHandler.result());
            } else {
              LOGGER.error(dbHandler.cause());
            }
          });

    } else if ("SUBS_APPEND".equals(eventType)) {
      databaseOperations(queries)
          .onComplete(dbHandler -> {
            if (dbHandler.succeeded()) {
              promise.complete(dbHandler.result());
            } else {
              LOGGER.error(dbHandler.cause());
            }
          });

    } else {
      LOGGER.error("Invelid event type [{}] for subscription message", eventType);
    }
  }

  private void executePostgresQuery(Future<JsonObject> postgresService,
                                    Promise<JsonObject> promise) {
    Future<JsonObject> futureResult = postgresService;
    futureResult.onComplete(handler -> {
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

  @Override
  public Future<Void> processSubscriptionMonitoringMessages(JsonObject message) {
    subsAuditService.generateAuditLog(message.getString("id"), message);
    return Future.succeededFuture();
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
