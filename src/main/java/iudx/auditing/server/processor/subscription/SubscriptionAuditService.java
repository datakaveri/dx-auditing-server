package iudx.auditing.server.processor.subscription;

import io.vertx.core.json.JsonObject;

public interface SubscriptionAuditService {
  public void addSubsConsumer(SubscriptionUser subObserver);
  public void generateAuditLog(String resourceid,JsonObject consumedMessage);
  public void deleteSubsConsumer(String subsId);
  
}
