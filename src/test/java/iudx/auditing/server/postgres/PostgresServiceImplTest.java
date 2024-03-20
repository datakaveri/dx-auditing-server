package iudx.auditing.server.postgres;

import static iudx.auditing.server.common.Constants.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import io.vertx.pgclient.PgPool;
import io.vertx.sqlclient.*;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.stubbing.Answer;

@ExtendWith({VertxExtension.class, MockitoExtension.class})
public class PostgresServiceImplTest {
  static PgPool pgClient;
  static PostgresServiceImpl postgresService;

  @BeforeAll
  static void setup(VertxTestContext vertxTestContext) {
    pgClient = mock(PgPool.class);
    postgresService = new PostgresServiceImpl(pgClient);
    vertxTestContext.completeNow();
  }

  @Test
  void testExecuteWriteQuery4Failure(VertxTestContext vertxTestContext) {

    JsonObject query =
        new JsonObject().put(ORIGIN, "cat-server").put(PG_INSERT_QUERY_KEY, "dummy-query");
    Future future = mock(Future.class);
    AsyncResult<?> asyncruslt = mock(AsyncResult.class);
    when(pgClient.withConnection(any())).thenReturn(future);

    doAnswer(
            new Answer<AsyncResult<?>>() {
              @Override
              public AsyncResult<?> answer(InvocationOnMock arg0) throws Throwable {
                ((Handler<AsyncResult<?>>) arg0.getArgument(0)).handle(asyncruslt);
                return null;
              }
            })
        .when(future)
        .onComplete(any());

    Future<JsonObject> resultJosn = postgresService.executeWriteQuery(query);
    vertxTestContext.completeNow();
  }

  @Test
  void testExecuteWriteQueryWithEmptyQuery(VertxTestContext vertxTestContext) {

    JsonObject query = new JsonObject().put(ORIGIN, "cat-server").put(PG_INSERT_QUERY_KEY, "");

    Future<JsonObject> result = postgresService.executeWriteQuery(query);
    result.onComplete(
        (Handler<AsyncResult<JsonObject>>)
            result
                .onSuccess(
                    handler -> {
                      vertxTestContext.failNow("Succeeded for a incorrect input");
                    })
                .onFailure(
                    handler -> {
                      assertEquals(
                          "Could not execute write query as the query supplied is blank or null",
                          handler.getMessage());
                      vertxTestContext.completeNow();
                    }));
  }

  @Test
  void testExecuteWriteQuery4Sucess(VertxTestContext vertxTestContext) {

    JsonObject query =
        new JsonObject().put(ORIGIN, "cat-server").put(PG_INSERT_QUERY_KEY, "dummy-query");

    Future future = mock(Future.class);
    SqlClient sqlClient = mock(SqlClient.class);
    AsyncResult<?> asyncruslt = mock(AsyncResult.class);
    SqlConnection connection = mock(SqlConnection.class);

    when(pgClient.withConnection(any())).thenReturn(future);
    when(asyncruslt.succeeded()).thenReturn(true);

    doAnswer(
            new Answer<AsyncResult<?>>() {
              @Override
              public AsyncResult<?> answer(InvocationOnMock arg0) throws Throwable {
                ((Handler<AsyncResult<?>>) arg0.getArgument(0)).handle(asyncruslt);
                return null;
              }
            })
        .when(future)
        .onComplete(any());

    Future<JsonObject> resultJosn = postgresService.executeWriteQuery(query);
    assertEquals(resultJosn.result().getString(RESULT), "Postgres Table Updated Successfully");
    vertxTestContext.completeNow();
  }

  @Test
  void testExecuteWriteQuery4Sucess2(VertxTestContext vertxTestContext) {

    JsonObject query =
        new JsonObject().put(ORIGIN, "cat-server").put(PG_DELETE_QUERY_KEY, "dummy-query");

    Future future = mock(Future.class);
    SqlClient sqlClient = mock(SqlClient.class);
    AsyncResult<?> asyncruslt = mock(AsyncResult.class);
    SqlConnection connection = mock(SqlConnection.class);

    when(pgClient.withConnection(any())).thenReturn(future);
    when(asyncruslt.succeeded()).thenReturn(true);

    doAnswer(
            new Answer<AsyncResult<?>>() {
              @Override
              public AsyncResult<?> answer(InvocationOnMock arg0) throws Throwable {
                ((Handler<AsyncResult<?>>) arg0.getArgument(0)).handle(asyncruslt);
                return null;
              }
            })
        .when(future)
        .onComplete(any());

    Future<JsonObject> resultJosn = postgresService.executeDeleteQuery(query);
    assertEquals(resultJosn.result().getString(RESULT), "Postgres Table row deleted Successfully");
    vertxTestContext.completeNow();
  }

  @Test
  void testExecuteWriteQuery4Failure2(VertxTestContext vertxTestContext) {

    JsonObject query =
        new JsonObject().put(ORIGIN, "cat-server").put(PG_DELETE_QUERY_KEY, "dummy-query");
    Future future = mock(Future.class);
    AsyncResult<?> asyncruslt = mock(AsyncResult.class);
    when(pgClient.withConnection(any())).thenReturn(future);

    doAnswer(
            new Answer<AsyncResult<?>>() {
              @Override
              public AsyncResult<?> answer(InvocationOnMock arg0) throws Throwable {
                ((Handler<AsyncResult<?>>) arg0.getArgument(0)).handle(asyncruslt);
                return null;
              }
            })
        .when(future)
        .onComplete(any());

    Future<JsonObject> resultJosn = postgresService.executeDeleteQuery(query);
    vertxTestContext.completeNow();
  }

  @Test
  void testExecuteWriteQueryWithEmptyQuery2(VertxTestContext vertxTestContext) {

    JsonObject query = new JsonObject().put(ORIGIN, "cat-server").put(PG_DELETE_QUERY_KEY, "");

    Future<JsonObject> result = postgresService.executeDeleteQuery(query);
    result.onComplete(
        (Handler<AsyncResult<JsonObject>>)
            result
                .onSuccess(
                    handler -> {
                      vertxTestContext.failNow("Succeeded for a incorrect input");
                    })
                .onFailure(
                    handler -> {
                      assertEquals(
                          "Could not execute delete query as the query supplied is blank or null",
                          handler.getMessage());
                      vertxTestContext.completeNow();
                    }));
  }
}
