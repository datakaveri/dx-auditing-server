package iudx.auditing.server.processor.subscription;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;


@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class SubscriptionUser {
  @JsonProperty("user_id")
  private String userId;
  @JsonProperty("queue_name")
  private String subsId;
  @JsonProperty("entity")
  private String resourceId;
  @JsonProperty("provider_id")
  private String providerId;
  @JsonProperty("resource_group")
  private String resourceGroup;
  @JsonProperty("delegator_id")
  private String delegatorId;
  @JsonProperty("item_type")
  private String type;

  public SubscriptionUser() {
    super();
  }

  public String getUserId() {
    return userId;
  }

  public void setUserId(String userId) {
    this.userId = userId;
  }

  public String getSubsId() {
    return subsId;
  }

  public void setSubsId(String subsId) {
    this.subsId = subsId;
  }

  public String getResourceId() {
    return resourceId;
  }

  public void setResourceId(String resourceId) {
    this.resourceId = resourceId;
  }

  public String getProviderId() {
    return providerId;
  }

  public void setProviderId(String providerId) {
    this.providerId = providerId;
  }

  public String getResourceGroup() {
    return resourceGroup;
  }

  public void setResourceGroup(String resourceGroup) {
    this.resourceGroup = resourceGroup;
  }

  public String getDelegatorId() {
    return delegatorId;
  }

  public void setDelegatorId(String delegatorId) {
    this.delegatorId = delegatorId;
  }

  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }

  @Override
  public String toString() {
    return "SubscriptionUser{" + "userId='" + userId + '\'' + ", subsId='" + subsId + '\''
        + ", resourceId='" + resourceId + '\'' + ", providerId='" + providerId + '\''
        + ", resourceGroup='" + resourceGroup + '\'' + ", delegatorId='" + delegatorId + '\''
        + ", type='" + type + '\'' + '}';
  }
}
