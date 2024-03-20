package iudx.auditing.server.processor.subscription;

import io.vertx.core.Future;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import iudx.auditing.server.cache.CacheService;
import iudx.auditing.server.rabbitmq.RabbitMqService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class SubscriptionAuditServiceImplTest {

    @Mock
    private RabbitMqService rabbitMqService;

    @Mock
    private CacheService cacheService;

    private SubscriptionAuditServiceImpl subscriptionAuditService;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        subscriptionAuditService = new SubscriptionAuditServiceImpl(rabbitMqService, cacheService);
    }

    @Test
    public void testGenerateAuditLog() {
        // Arrange
        String resourceId = "testResourceId";
        JsonObject consumedMessage = new JsonObject();
        consumedMessage.put("id", resourceId);

        SubscriptionUser user = new SubscriptionUser();
        user.setUserId("testUserId");
        user.setSubsId("testSubsId");
        user.setResourceId("testResourceId");
        user.setProviderId("testProviderId");
        user.setResourceGroup("testResourceGroup");
        user.setDelegatorId("testDelegatorId");
        user.setType("testType");

        JsonObject userJson = JsonObject.mapFrom(user);
        JsonArray users = new JsonArray().add(userJson);
        JsonObject cacheResult = new JsonObject().put("results", users);

        when(cacheService.get(resourceId)).thenReturn(Future.succeededFuture(cacheResult));

        // Act
        Future<Void> result = subscriptionAuditService.generateAuditLog(consumedMessage);

        // Assert
        verify(rabbitMqService, times(1)).publishMessage(any());
        assertTrue(result.succeeded());
    }

    @Test
    public void testGenerateAuditLogFail() {
        // Arrange
        String resourceId = "testResourceId";
        JsonObject consumedMessage = new JsonObject();
        consumedMessage.put("id", resourceId);

        SubscriptionUser user = new SubscriptionUser();
        user.setUserId("testUserId");
        user.setSubsId("testSubsId");
        user.setResourceId("testResourceId");
        user.setProviderId("testProviderId");
        user.setResourceGroup("testResourceGroup");
        user.setDelegatorId("testDelegatorId");
        user.setType("testType");

        JsonObject userJson = JsonObject.mapFrom(user);
        JsonArray users = new JsonArray().add(userJson);
        JsonObject cacheResult = new JsonObject().put("results", users);

        when(cacheService.get(resourceId)).thenReturn(Future.failedFuture("failed"));

        // Act
        Future<Void> result = subscriptionAuditService.generateAuditLog(consumedMessage);

        // Assert
       // verify(rabbitMqService, times(1)).publishMessage(any());
        assertFalse(result.succeeded());
    }
}
