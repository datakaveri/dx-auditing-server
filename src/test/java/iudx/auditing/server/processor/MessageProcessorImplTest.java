package iudx.auditing.server.processor;

import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import iudx.auditing.server.immudb.ImmudbService;
import iudx.auditing.server.postgres.PostgresService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static iudx.auditing.server.common.Constants.ORIGIN;
import static iudx.auditing.server.querystrategy.util.Constants.*;
import static javax.xml.transform.OutputKeys.METHOD;
import static org.junit.jupiter.api.Assertions.assertEquals;
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


    @BeforeEach
    public void setUp(VertxTestContext vertxTestContext) {
        config = mock(JsonObject.class);
        messageProcessor = new MessageProcessorImpl(postgresService, immudbService, config);
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
                .put(RS_IMMUDB_TABLE_NAME, "RS_IMMUDB_TABLE_NAME");

        when(config.getString((anyString()))).thenReturn("tableName");
        doAnswer(Answer -> Future.succeededFuture()).when(postgresService).executeWriteQuery(any());
        doAnswer(Answer -> Future.succeededFuture()).when(immudbService).executeWriteQuery(any());

        Future<JsonObject> resultJson = messageProcessor.process(message);
        assertEquals(message, resultJson.result());
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

        Future<JsonObject> resultJson = messageProcessor.process(message);
        assertEquals(message, resultJson.result());
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

        Future<JsonObject> resultJson = messageProcessor.process(message);
        assertEquals(message, resultJson.result());
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

        Future<JsonObject> resultJson = messageProcessor.process(message);
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
        Future<JsonObject> resultJson = messageProcessor.process(message);
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
                .put(RS_IMMUDB_TABLE_NAME, "RS_IMMUDB_TABLE_NAME");

        when(config.getString((anyString()))).thenReturn("tableName");

        doAnswer(Answer -> Future.succeededFuture()).when(postgresService).executeWriteQuery(any());
        doAnswer(Answer -> Future.failedFuture("failed")).when(immudbService).executeWriteQuery(any());
        doAnswer(Answer -> Future.succeededFuture()).when(postgresService).executeDeleteQuery(any());

        Future<JsonObject> resultJson = messageProcessor.process(message);
        assertEquals("failed", resultJson.cause().getMessage());
        vertxTestContext.completeNow();
    }

    @Test
    @DisplayName("Testing Success process as origin ogc-server")
    void testPocess4OgcSuccess(VertxTestContext vertxTestContext) {
        message = new JsonObject().put(ORIGIN, "ogc-server")
                .put(USER_ID, "userid")
                .put(PRIMARY_KEY, "primary_key")
                .put(ID, "id")
                .put(PROVIDER_ID, "providerId")
                .put(API, "api")
                .put(EPOCH_TIME, 5000)
                .put(ISO_TIME, "2000-03-03T21:00:00Z")
                .put(SIZE, 0)
                .put(OGC_PG_TABLE_NAME, "OGC_PG_TABLE_NAME")
                .put(OGC_IMMUDB_TABLE_NAME, "OGC_IMMUDB_TABLE_NAME")
                .put(ITEM_TYPE,"itemType");

        when(config.getString((anyString()))).thenReturn("auditing_ogc");
        doAnswer(Answer -> Future.succeededFuture()).when(postgresService).executeWriteQuery(any());
        doAnswer(Answer -> Future.succeededFuture()).when(immudbService).executeWriteQuery(any());

        Future<JsonObject> resultJson = messageProcessor.process(message);
        assertEquals(message, resultJson.result());
        vertxTestContext.completeNow();
    }

}