package iudx.auditing.server.processor.subscription;

import io.vertx.core.json.JsonObject;

public class SubscriptionAuditMessage {

  private long epoch;
  private String isoTime;
  private String userid;
  private String id;
  private long responseSize;
  private String providerId;
  private String primaryKey;
  private String origin;
  private String api;
  private String resourceGroup;
  private String delegatorId;
  private String type;


  public SubscriptionAuditMessage(Builder builder) {
    this.epoch = builder.epoch;
    this.isoTime = builder.isoTime;
    this.userid = builder.userid;
    this.id = builder.id;
    this.responseSize = builder.responseSize;
    this.providerId = builder.providerId;
    this.primaryKey = builder.primaryKey;
    this.origin = builder.origin;
    this.api = "/ngsi-ld/v1/subscription";
    this.resourceGroup = builder.resourceGroup;
    this.delegatorId = builder.delegatorId;
    this.type = builder.type;
  }


  public JsonObject toJson() {
    JsonObject json = new JsonObject();
    json.put("primaryKey", primaryKey);
    json.put("userid", userid);
    json.put("id", id);
    json.put("resourceGroup", resourceGroup);
    json.put("providerID", providerId);
    json.put("delegatorId", delegatorId);
    json.put("type", type);
    json.put("api", api);
    json.put("epochTime", epoch);
    json.put("isoTime", isoTime);
    json.put("response_size", responseSize);
    json.put("origin", origin);

    return json;
    // TODO Auto-generated method stub
  }

  public static class Builder {
    private long epoch;
    private String isoTime;
    private String userid;
    private String id;
    private long responseSize;
    private String providerId;
    private String primaryKey;
    private String origin;
    private String resourceGroup;
    private String delegatorId;
    private String type;

    public Builder atEpoch(long epoch) {
      this.epoch = epoch;
      return this;
    }

    public Builder atIsoTime(String isoTime) {
      this.isoTime = isoTime;
      return this;
    }

    public Builder forUserId(String userId) {
      this.userid = userId;
      return this;
    }

    public Builder forResourceId(String id) {
      this.id = id;
      return this;
    }

    public Builder withResponseSize(long responseSize) {
      this.responseSize = responseSize;
      return this;
    }

    public Builder withProviderId(String providerId) {
      this.providerId = providerId;
      return this;
    }

    public Builder withPrimaryKey(String primaryKey) {
      this.primaryKey = primaryKey;
      return this;
    }

    public Builder forOrigin(String origin) {
      this.origin = origin;
      return this;
    }

    public Builder forResourceGroup(String resourceGroup) {
      this.resourceGroup = resourceGroup;
      return this;
    }

    public Builder withDelegatorId(String delegatorId) {
      this.delegatorId = delegatorId;
      return this;
    }

    public Builder forType(String type) {
      this.type = type;
      return this;
    }

    public SubscriptionAuditMessage build() {
      return new SubscriptionAuditMessage(this);
    }

  }

}
