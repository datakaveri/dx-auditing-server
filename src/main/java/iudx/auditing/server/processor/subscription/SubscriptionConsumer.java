package iudx.auditing.server.processor.subscription;

import iudx.auditing.server.rabbitmq.RabbitMqService;

public interface SubscriptionConsumer {
  public void publishAuditLogMessage(SubscriptionAuditMessage auditMessage, RabbitMqService rabbitMqService);
  
}
