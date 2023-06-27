package iudx.auditing.server.common;

import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;


import static iudx.auditing.server.common.VirtualHosts.*;
import static org.junit.jupiter.api.Assertions.*;


@ExtendWith(VertxExtension.class)
class VHostsTest {
    @Test
    public void test(VertxTestContext testContext) {
        assertNotNull(IUDX_EXTERNAL.value);
        assertNotNull(IUDX_PROD.value);
        assertNotNull(IUDX_INTERNAL.value);
        testContext.completeNow();
    }
}

