package iudx.auditing.server.processor.subscription;

public interface SubscriptionConsumer {
  public void publishAuditLogMessage(SubscriptionAuditMessage auditMessage);
  
}
