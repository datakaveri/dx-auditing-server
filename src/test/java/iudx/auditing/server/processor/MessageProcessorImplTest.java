package iudx.auditing.server.processor;

import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import iudx.auditing.server.immudb.ImmudbService;
import iudx.auditing.server.postgres.PostgresService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

@ExtendWith({VertxExtension.class, MockitoExtension.class})
class MessageProcessorImplTest {

    @Mock
    PostgresService postgresService;
    @Mock
    ImmudbService immudbService;
    @Mock
    JsonObject config;
    MessageProcessorImpl messageProcessor;
    Vertx vertx;

    @BeforeEach
    public void setUp(VertxTestContext vertxTestContext) {
        JsonObject config = mock(JsonObject.class);
        messageProcessor = new MessageProcessorImpl(vertx, postgresService, immudbService, config);
        vertxTestContext.completeNow();
    }

    @Test
    void testPocess(VertxTestContext vertxTestContext) {

        vertxTestContext.completeNow();
    }
}