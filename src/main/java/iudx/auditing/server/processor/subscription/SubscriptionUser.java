package iudx.auditing.server.processor.subscription;


import iudx.auditing.server.rabbitmq.RabbitMqService;

public class SubscriptionUser implements SubscriptionConsumer {

  private final String userId;
  private final String subsId;
  private final String resourceId;
  private final String providerId;
  private final String resourceGroup;
  private final String delegatorId;
  private final String type;

  public SubscriptionUser(final String userId, final String subsId, final String resourceId,
                          final String providerId, final String resourceGroup, String delegatorId,
                          String type) {
    //null check
    this.userId = userId;
    this.subsId = subsId;
    this.resourceId = resourceId;
    this.providerId = providerId;
    this.resourceGroup = resourceGroup;
    this.delegatorId = delegatorId;
    this.type = type;
  }

  public String getType() {
    return type;
  }

  public String getProviderId() {
    return providerId;
  }

  public String getResourceGroup() {
    return resourceGroup;
  }

  public String getDelegatorId() {
    return delegatorId;
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
  public void publishAuditLogMessage(SubscriptionAuditMessage auditMessage,
                                     RabbitMqService rabbitMqService) {

    rabbitMqService.publishMessage(auditMessage.toJson());
  }

  @Override
  public String toString() {
    return "SubscriptionUser{" +
        "userId='" + userId + '\'' +
        ", subsId='" + subsId + '\'' +
        ", resourceId='" + resourceId + '\'' +
        ", providerId='" + providerId + '\'' +
        ", resourceGroup='" + resourceGroup + '\'' +
        ", delegatorId='" + delegatorId + '\'' +
        ", type='" + type + '\'' +
        '}';
  }
}
