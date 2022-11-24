package iudx.auditing.server.immudb;

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Promise;
import io.vertx.core.json.JsonObject;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import io.vertx.pgclient.PgPool;
import io.vertx.sqlclient.SqlClient;
import io.vertx.sqlclient.SqlConnection;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.stubbing.Answer;

import static iudx.auditing.server.common.Constants.RESULT;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith({VertxExtension.class, MockitoExtension.class})
class ImmudbServiceImplTest {

    static PgPool pgClient;
    static ImmudbService immudbService;

    @BeforeAll
    static void setup(VertxTestContext vertxTestContext) {
        pgClient = mock(PgPool.class);
        immudbService = new ImmudbServiceImpl(pgClient);
        vertxTestContext.completeNow();
    }

    @Test
    void testExecuteWriteQuery4Failure(VertxTestContext vertxTestContext) {
        JsonObject query = mock(JsonObject.class);
        Future future = mock(Future.class);
        AsyncResult<?> asyncruslt = mock(AsyncResult.class);

        when(pgClient.withConnection(any())).thenReturn(future);

        doAnswer(new Answer<AsyncResult<?>>() {
            @Override
            public AsyncResult<?> answer(InvocationOnMock arg0) throws Throwable {
                ((Handler<AsyncResult<?>>) arg0.getArgument(0)).handle(asyncruslt);
                return null;
            }
        }).when(future).onComplete(any());

        Future<JsonObject> resultJosn = immudbService.executeWriteQuery(query);
        vertxTestContext.completeNow();
    }

    @Test
    void testExecuteWriteQuery4Sucess(VertxTestContext vertxTestContext) {
        JsonObject query = mock(JsonObject.class);
        Future future = mock(Future.class);
        SqlClient sqlClient = mock(SqlClient.class);
        AsyncResult<?> asyncruslt = mock(AsyncResult.class);
        SqlConnection connection = mock(SqlConnection.class);

        when(pgClient.withConnection(any())).thenReturn(future);
        when(asyncruslt.succeeded()).thenReturn(true);

        doAnswer(new Answer<AsyncResult<?>>() {
            @Override
            public AsyncResult<?> answer(InvocationOnMock arg0) throws Throwable {
                ((Handler<AsyncResult<?>>) arg0.getArgument(0)).handle(asyncruslt);
                return null;
            }
        }).when(future).onComplete(any());


        Future<JsonObject> resultJosn = immudbService.executeWriteQuery(query);
        assertEquals(resultJosn.result().getString(RESULT), "Table Updated Successfully");
        vertxTestContext.completeNow();
    }
}