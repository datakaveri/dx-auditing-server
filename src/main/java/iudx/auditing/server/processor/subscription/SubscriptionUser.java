package iudx.auditing.server.processor.subscription;


public class SubscriptionUser implements SubscriptionConsumer{

  private final String userId;
  private final String subsId;
  private final String resourceId;
  
  public SubscriptionUser(final String userId,final String subsId,final String resourceId) {
    //null check
    this.userId=userId;
    this.subsId=subsId;
    this.resourceId=resourceId;
  }
  
  public String getUserId() {
    return userId;
  }

  public String getSubsId() {
    return subsId;
  }

  public String getResourceId() {
    return resourceId;
  }

  @Override
  public void publishAuditLogMessage(SubscriptionAuditMessage auditMessage) {
    // TODO puch Audit message to RMQ
    
  }

}
