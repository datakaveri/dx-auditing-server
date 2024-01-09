package iudx.auditing.server.processor;

import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import iudx.auditing.server.cache.CacheService;
import iudx.auditing.server.immudb.ImmudbService;
import iudx.auditing.server.postgres.PostgresService;
import iudx.auditing.server.processor.subscription.SubscriptionAuditService;
import iudx.auditing.server.processor.subscription.SubscriptionAuditServiceImpl;
import iudx.auditing.server.rabbitmq.RabbitMqService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static iudx.auditing.server.common.Constants.*;
import static iudx.auditing.server.querystrategy.util.Constants.*;
import static javax.xml.transform.OutputKeys.METHOD;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith({VertxExtension.class, MockitoExtension.class})
class MessageProcessorImplTest {

    @Mock
    PostgresService postgresService;
    @Mock
    ImmudbService immudbService;
    @Mock
    JsonObject config;
    MessageProcessorImpl messageProcessor;
    JsonObject message;
    RabbitMqService rabbitMqService;
    CacheService cacheService;
    SubscriptionAuditService subscriptionAuditService;


    @BeforeEach
    public void setUp(VertxTestContext vertxTestContext) {
        config = mock(JsonObject.class);
        messageProcessor =
            new MessageProcessorImpl(postgresService, immudbService, subscriptionAuditService, config,cacheService);
        vertxTestContext.completeNow();
    }

    @Test
    @DisplayName("Testing Success process as origin rs-server")
    void testPocess4RsSuccess(VertxTestContext vertxTestContext) {
        message = new JsonObject().put(ORIGIN, "rs-server")
            .put(USER_ID, "userid")
            .put(PRIMARY_KEY, "prmary_key")
            .put(ID, "id")
            .put(PROVIDER_ID, "providerId")
            .put(API, "api")
            .put(EPOCH_TIME, 5000)
            .put(ISO_TIME, "2000-03-03T21:00:00Z")
            .put(SIZE, 0)
            .put(RS_PG_TABLE_NAME, "RS_PG_TABLE_NAME")
            .put(RS_IMMUDB_TABLE_NAME, "RS_IMMUDB_TABLE_NAME")
            .put(RESOURCE_GROUP, "resourceGroup")
            .put(TYPE, "type")
            .put(DELEGATOR_ID, "del");

        when(config.getString((anyString()))).thenReturn("tableName");
        doAnswer(Answer -> Future.succeededFuture()).when(postgresService).executeWriteQuery(any());
        doAnswer(Answer -> Future.succeededFuture()).when(immudbService).executeWriteQuery(any());

        Future<JsonObject> resultJson = messageProcessor.processAuditEventMessages(message);
        assertTrue(resultJson.result().containsKey(DELIVERY_TAG));
        assertTrue(resultJson.result().containsKey(PG_INSERT_QUERY_KEY));
        assertTrue(resultJson.result().containsKey(PG_DELETE_QUERY_KEY));
        assertTrue(resultJson.result().containsKey(IMMUDB_WRITE_QUERY));
        vertxTestContext.completeNow();
    }

    @Test
    @DisplayName("Testing Success process as origin cat-server")
    void testPocess4CatSuccess(VertxTestContext vertxTestContext) {
        message = new JsonObject()
                .put(ORIGIN, "cat-server")
                .put(PRIMARY_KEY, "prmary_key")
                .put(USER_ID, "userid")
                .put(IID, "iid")
                .put(API, "api")
                .put(HTTP_METHOD, "httpMethod")
                .put(IUDX_ID, "iudxId")
                .put(EPOCH_TIME, 5000)
                .put(CAT_PG_TABLE_NAME, "tableName")
                .put(USER_ROLE, "userRole");

        when(config.getString((anyString()))).thenReturn("tableName");
        doAnswer(Answer -> Future.succeededFuture()).when(postgresService).executeWriteQuery(any());
        doAnswer(Answer -> Future.succeededFuture()).when(immudbService).executeWriteQuery(any());

        Future<JsonObject> resultJson = messageProcessor.processAuditEventMessages(message);
        assertTrue(resultJson.result().containsKey(DELIVERY_TAG));
        assertTrue(resultJson.result().containsKey(PG_INSERT_QUERY_KEY));
        assertTrue(resultJson.result().containsKey(PG_DELETE_QUERY_KEY));
        assertTrue(resultJson.result().containsKey(IMMUDB_WRITE_QUERY));
        vertxTestContext.completeNow();
    }

    @Test
    @DisplayName("Testing Success process as origin auth-server")
    void testPocess4AuthSuccess(VertxTestContext vertxTestContext) {

        message = new JsonObject()
                .put(ORIGIN, "auth-server")
                .put(PRIMARY_KEY, "prmary_key")
                .put(BODY, new JsonObject())
                .put(IID, "iid")
                .put(API, "api")
                .put(METHOD, "method")
                .put(EPOCH_TIME, 5000)
                .put(USER_ID, "userid")
                .put(AUTH_PG_TABLE_NAME, "tableName");

        when(config.getString((anyString()))).thenReturn("tableName");
        doAnswer(Answer -> Future.succeededFuture()).when(postgresService).executeWriteQuery(any());
        doAnswer(Answer -> Future.succeededFuture()).when(immudbService).executeWriteQuery(any());

        Future<JsonObject> resultJson = messageProcessor.processAuditEventMessages(message);
        assertTrue(resultJson.result().containsKey(DELIVERY_TAG));
        assertTrue(resultJson.result().containsKey(PG_INSERT_QUERY_KEY));
        assertTrue(resultJson.result().containsKey(PG_DELETE_QUERY_KEY));
        assertTrue(resultJson.result().containsKey(IMMUDB_WRITE_QUERY));
        vertxTestContext.completeNow();
    }

    @Test
    @DisplayName("Testing Failed process as origin auth-server")
    void testPocess4AuthFailure(VertxTestContext vertxTestContext) {
        message = new JsonObject()
                .put(ORIGIN, "auth-server")
                .put(PRIMARY_KEY, "prmary_key")
                .put(BODY, new JsonObject())
                .put(IID, "iid")
                .put(API, "api")
                .put(METHOD, "method")
                .put(EPOCH_TIME, 5000)
                .put(USER_ID, "userid")
                .put(AUTH_IMMUDB_TABLE_NAME, "tableName");

        when(config.getString((anyString()))).thenReturn("tableName");
        doAnswer(Answer -> Future.succeededFuture()).when(postgresService).executeWriteQuery(any());
        doAnswer(Answer -> Future.failedFuture("failed")).when(immudbService).executeWriteQuery(any());
        doAnswer(Answer -> Future.succeededFuture()).when(postgresService).executeDeleteQuery(any());

        Future<JsonObject> resultJson = messageProcessor.processAuditEventMessages(message);
        assertEquals("failed", resultJson.cause().getMessage());
        vertxTestContext.completeNow();
    }

    @Test
    @DisplayName("Testing Failed process as origin cat-server")
    void testPocess4CatFailure(VertxTestContext vertxTestContext) {
        message = new JsonObject()
                .put(ORIGIN, "cat-server")
                .put(PRIMARY_KEY, "prmary_key")
                .put(USER_ID, "userid")
                .put(IID, "iid")
                .put(API, "api")
                .put(HTTP_METHOD, "httpMethod")
                .put(IUDX_ID, "iudxId")
                .put(EPOCH_TIME, 5000)
                .put(CAT_PG_TABLE_NAME, "tableName")
                .put(USER_ROLE, "userRole");
        when(config.getString((anyString()))).thenReturn("tableName");

        doAnswer(Answer -> Future.succeededFuture()).when(postgresService).executeWriteQuery(any());
        doAnswer(Answer -> Future.failedFuture("failed")).when(immudbService).executeWriteQuery(any());
        doAnswer(Answer -> Future.succeededFuture()).when(postgresService).executeDeleteQuery(any());
        Future<JsonObject> resultJson = messageProcessor.processAuditEventMessages(message);
        assertEquals("failed", resultJson.cause().getMessage());
        vertxTestContext.completeNow();
    }

    @Test
    @DisplayName("Testing Failed process as origin rs-server")
    void testPocess4RsFailure(VertxTestContext vertxTestContext) {
        message = new JsonObject().put(ORIGIN, "rs-server")
            .put(USER_ID, "userid")
            .put(PRIMARY_KEY, "prmary_key")
            .put(ID, "id")
            .put(PROVIDER_ID, "providerId")
            .put(API, "api")
            .put(EPOCH_TIME, 5000)
            .put(ISO_TIME, "2000-03-03T21:00:00Z")
            .put(SIZE, 0)
            .put(RS_PG_TABLE_NAME, "RS_PG_TABLE_NAME")
            .put(RS_IMMUDB_TABLE_NAME, "RS_IMMUDB_TABLE_NAME")
            .put(RESOURCE_GROUP, "resourceGroup")
            .put(TYPE, "type")
            .put(DELEGATOR_ID, "delegator");

        when(config.getString((anyString()))).thenReturn("tableName");

        doAnswer(Answer -> Future.succeededFuture()).when(postgresService).executeWriteQuery(any());
        doAnswer(Answer -> Future.failedFuture("failed")).when(immudbService).executeWriteQuery(any());
        doAnswer(Answer -> Future.succeededFuture()).when(postgresService).executeDeleteQuery(any());

        Future<JsonObject> resultJson = messageProcessor.processAuditEventMessages(message);
        assertEquals("failed", resultJson.cause().getMessage());
        vertxTestContext.completeNow();
    }
    @Test
    @DisplayName("Testing Success process as origin acl-apd-server")
    void testPocess4AclApdSuccess(VertxTestContext vertxTestContext) {
        message = new JsonObject().put(ORIGIN, ACL_APD_SERVER)
            .put(USER_ID, "49b52be3-bc00-4548-97d7-99cee5bfc8cd")
            .put(PRIMARY_KEY, "PRIMARY_KEY")
            .put(API, "api")
            .put(EPOCH_TIME, 5000)
            .put(ISO_TIME, "2000-03-03T21:00:00Z")
            .put(SIZE, 0)
            .put(HTTP_METHOD,"post")
            .put(BODY,new JsonObject())
            .put(APD_WRITE_QUERY_PG, "APD_PG_TABLE_NAME")
            .put(APD_IMMUDB_TABLE_NAME, "APD_IMMUDB_TABLE_NAME");

        when(config.getString((anyString()))).thenReturn("tablename");
        doAnswer(Answer -> Future.succeededFuture()).when(postgresService).executeWriteQuery(any());
        doAnswer(Answer -> Future.succeededFuture()).when(immudbService).executeWriteQuery(any());

        Future<JsonObject> resultJson = messageProcessor.processAuditEventMessages(message);
        assertTrue(resultJson.result().containsKey(DELIVERY_TAG));
        assertTrue(resultJson.result().containsKey(PG_INSERT_QUERY_KEY));
        assertTrue(resultJson.result().containsKey(PG_DELETE_QUERY_KEY));
        assertTrue(resultJson.result().containsKey(IMMUDB_WRITE_QUERY));
        vertxTestContext.completeNow();
    }

    @Test
    @DisplayName("Testing Success process as origin rs-server")
    void testPocess4RsSuccess4Created(VertxTestContext vertxTestContext) {
        message = new JsonObject().put(ORIGIN, "rs-server")
            .put(USER_ID, "userid")
            .put(PRIMARY_KEY, "prmary_key")
            .put(ID, "id")
            .put(PROVIDER_ID, "providerId")
            .put(API, "api")
            .put(EPOCH_TIME, 5000)
            .put(ISO_TIME, "2000-03-03T21:00:00Z")
            .put(SIZE, 0)
            .put(RS_PG_TABLE_NAME, "RS_PG_TABLE_NAME")
            .put(RS_IMMUDB_TABLE_NAME, "RS_IMMUDB_TABLE_NAME")
            .put(RESOURCE_GROUP, "resourceGroup")
            .put(TYPE, "type")
            .put(DELEGATOR_ID, "del")
                .put("eventType","SUBS_CREATED")
                    .put("subscriptionID","subscriptionID");

        when(config.getString((anyString()))).thenReturn("");
        doAnswer(Answer -> Future.succeededFuture()).when(postgresService).executeWriteQuery(any());
        doAnswer(Answer -> Future.succeededFuture()).when(immudbService).executeWriteQuery(any());

        Future<JsonObject> resultJson = messageProcessor.processAuditEventMessages(message);
        assertTrue(resultJson.result().containsKey(DELIVERY_TAG));
        assertTrue(resultJson.result().containsKey(PG_INSERT_QUERY_KEY));
        assertTrue(resultJson.result().containsKey(PG_DELETE_QUERY_KEY));
        assertTrue(resultJson.result().containsKey(IMMUDB_WRITE_QUERY));
        vertxTestContext.completeNow();
    }
    @Test
    @DisplayName("Testing Success process as origin rs-server")
    void testPocess4RsSuccess4Deleted(VertxTestContext vertxTestContext) {
        message = new JsonObject().put(ORIGIN, "rs-server")
            .put(USER_ID, "userid")
            .put(PRIMARY_KEY, "prmary_key")
            .put(ID, "id")
            .put(PROVIDER_ID, "providerId")
            .put(API, "api")
            .put(EPOCH_TIME, 5000)
            .put(ISO_TIME, "2000-03-03T21:00:00Z")
            .put(SIZE, 0)
            .put(RS_PG_TABLE_NAME, "RS_PG_TABLE_NAME")
            .put(RS_IMMUDB_TABLE_NAME, "RS_IMMUDB_TABLE_NAME")
            .put(RESOURCE_GROUP, "resourceGroup")
            .put(TYPE, "type")
            .put(DELEGATOR_ID, "del")
            .put("eventType","SUBS_DELETED")
            .put("subscriptionID","SUBSDELETED");

        when(config.getString((anyString()))).thenReturn("tableName");
        doAnswer(Answer -> Future.succeededFuture()).when(postgresService).executeWriteQuery(any());
        doAnswer(Answer -> Future.succeededFuture()).when(immudbService).executeWriteQuery(any());

        Future<JsonObject> resultJson = messageProcessor.processAuditEventMessages(message);
        assertTrue(resultJson.result().containsKey(DELIVERY_TAG));
        assertTrue(resultJson.result().containsKey(PG_INSERT_QUERY_KEY));
        assertTrue(resultJson.result().containsKey(PG_DELETE_QUERY_KEY));
        assertTrue(resultJson.result().containsKey(IMMUDB_WRITE_QUERY));
        vertxTestContext.completeNow();
    }

    @Test
    @DisplayName("Testing Success process as origin rs-server")
    void testPocess4RsSuccess4Append(VertxTestContext vertxTestContext) {
        message = new JsonObject().put(ORIGIN, "rs-server")
            .put(USER_ID, "userid")
            .put(PRIMARY_KEY, "prmary_key")
            .put(ID, "id")
            .put(PROVIDER_ID, "providerId")
            .put(API, "api")
            .put(EPOCH_TIME, 5000)
            .put(ISO_TIME, "2000-03-03T21:00:00Z")
            .put(SIZE, 0)
            .put(RS_PG_TABLE_NAME, "RS_PG_TABLE_NAME")
            .put(RS_IMMUDB_TABLE_NAME, "RS_IMMUDB_TABLE_NAME")
            .put(RESOURCE_GROUP, "resourceGroup")
            .put(TYPE, "type")
            .put(DELEGATOR_ID, "del")
            .put("eventType","SUBS_APPEND")
            .put("subscriptionID","SUBSDELETED");

        when(config.getString((anyString()))).thenReturn("tableName");
        doAnswer(Answer -> Future.succeededFuture()).when(postgresService).executeWriteQuery(any());
        doAnswer(Answer -> Future.succeededFuture()).when(immudbService).executeWriteQuery(any());

        Future<JsonObject> resultJson = messageProcessor.processAuditEventMessages(message);
        assertTrue(resultJson.result().containsKey(DELIVERY_TAG));
        assertTrue(resultJson.result().containsKey(PG_INSERT_QUERY_KEY));
        assertTrue(resultJson.result().containsKey(PG_DELETE_QUERY_KEY));
        assertTrue(resultJson.result().containsKey(IMMUDB_WRITE_QUERY));
        vertxTestContext.completeNow();
    }

    @Test
    @DisplayName("Testing Success process as origin consent-log")
    void testPocess4ConsentSuccess(VertxTestContext vertxTestContext) {
        message = new JsonObject().put(ORIGIN, CONSENT_LOG_ADEX)
                .put(ITEM_ID, "49b52be3-bc00-4548-97d7-99cee5bfc8cd")
                .put(PRIMARY_KEY, "PRIMARY_KEY")
                .put(AIU_ID, "aiuid")
                .put(AIP_ID, "aipid")
                .put(ISO_TIME, "2000-03-03T21:00:00Z")
                .put(EVENT_TYPE, "DATA_SENT")
                .put(DP_ID,"dpid")
                .put(ARTIFACT_ID, "Artifactid")
                .put(ITEM_TYPE,"resource")
                .put(LOG_SIGN,"logsign")
                .put(BODY,new JsonObject())
                .put(CONSENT_LOG_PG_TABLE_NAME, "CONSENT_PG_TABLE_NAME")
                .put(CONSENT_LOG_IMMUDB_TABLE_NAME, "CONSENT_IMMUDB_TABLE_NAME");

        when(config.getString((anyString()))).thenReturn("tablename");
        doAnswer(Answer -> Future.succeededFuture()).when(postgresService).executeWriteQuery(any());
        doAnswer(Answer -> Future.succeededFuture()).when(immudbService).executeWriteQuery(any());

        Future<JsonObject> resultJson = messageProcessor.processAuditEventMessages(message);
        assertTrue(resultJson.result().containsKey(DELIVERY_TAG));
        assertTrue(resultJson.result().containsKey(PG_INSERT_QUERY_KEY));
        assertTrue(resultJson.result().containsKey(PG_DELETE_QUERY_KEY));
        assertTrue(resultJson.result().containsKey(IMMUDB_WRITE_QUERY));
        vertxTestContext.completeNow();
    }
}