package iudx.auditing.server.processor.subscription;

import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;
import iudx.auditing.server.cache.CacheService;
import iudx.auditing.server.rabbitmq.RabbitMqService;

public interface SubscriptionAuditService {

  Future<Void> generateAuditLog(String resourceid, JsonObject consumedMessage,
                                CacheService cacheService);
  //public void publishAuditLogMessage(SubscriptionAuditMessage auditMessage, RabbitMqService rabbitMqService);

}
