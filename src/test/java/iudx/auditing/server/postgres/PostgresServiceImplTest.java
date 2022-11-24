package iudx.auditing.server.postgres;

import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import io.vertx.pgclient.PgConnectOptions;
import io.vertx.pgclient.PgPool;
import io.vertx.sqlclient.PoolOptions;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers
@ExtendWith(MockitoExtension.class)
@ExtendWith(VertxExtension.class)
class PostgresServiceImplTest {
   /*
   private static final Logger LOGGER = LogManager.getLogger(PostgresServiceImplTest.class);
    static PostgresServiceImpl pgService;
    @Container
    static PostgreSQLContainer container = new PostgreSQLContainer<>("postgres:12.11")
            .withInitScript("pg_test.sql");

    @BeforeAll
    public static void  setUp(VertxTestContext vertxTestContext) {
        // Now we have an address and port for Postgresql, no matter where it is running
        Integer port = container.getFirstMappedPort();
        String host = container.getHost();
        String db = container.getDatabaseName();
        String user = container.getUsername();
        String password = container.getPassword();

        PgConnectOptions connectOptions = new PgConnectOptions()
                .setPort(port)
                .setHost(host)
                .setDatabase(db)
                .setUser(user)
                .setPassword(password);

        PoolOptions poolOptions = new PoolOptions()
                .setMaxSize(10);

        Vertx vertxObj = Vertx.vertx();

        PgPool pool = PgPool.pool(vertxObj, connectOptions, poolOptions);

        pgService = new PostgresServiceImpl(pool);
        vertxTestContext.completeNow();
    }

    @Test
    public void testExecuteQuerySuccess(VertxTestContext vertxTestContext) {
    String insert= "INSERT INTO tableName (id,api,userid,epochtime,resourceid,isotime,providerid,size) VALUES ('primary_key','api','userid',5000,'id','isoTime','providerid',0)";
    String delete = "DELETE FROM tableName WHERE id = 'primary_key";

    JsonObject jsonObject= new JsonObject().put("postgresInsertQuery",insert)
            .put("postgresDeleteQuery",delete);

        pgService.executeWriteQuery(jsonObject);
                vertxTestContext.completeNow();

    }

    */

}