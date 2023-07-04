package iudx.auditing.server.processor.subscription;

import io.vertx.core.json.JsonObject;

public class SubscriptionAuditMessage{
  
  private long epoch;
  private String isoTime;
  private String userid;
  private String id;
  private long responseSize;
  private String providerId;
  private String primaryKey;
  private String origin;
  private String api;
  
  
  
  public SubscriptionAuditMessage(Builder builder) {
    this.epoch=builder.epoch;
    this.isoTime=builder.isoTime;
    this.userid=builder.userid;
    this.id=builder.id;
    this.responseSize=builder.responseSize;
    this.providerId=builder.providerId;
    this.primaryKey=builder.primaryKey;
    this.origin=builder.origin;
    this.api="/ngsi-ld/v1/subscription";
  }


  public JsonObject toJson() {
    // TODO Auto-generated method stub
    return null;
  }
  
  public static class Builder{
    private long epoch;
    private String isoTime;
    private String userid;
    private String id;
    private long responseSize;
    private String providerId;
    private String primaryKey;
    private String origin;
    
    public Builder atEpoch(long epoch) {
      this.epoch=epoch;
      return this;
    }
    
    public Builder atIsoTime(String isoTime) {
      this.isoTime=isoTime;
      return this;
    }
    
    public Builder forUserId(String userId) {
      this.userid=userid;
      return this;
    }
    
    public Builder forResourceId(String id) {
      this.id=id;
      return this;
    }
    
    public Builder withResponseSize(long responseSize) {
      this.responseSize=responseSize;
      return this;
    }
    
    public Builder withProviderId(String providerId) {
      this.providerId=providerId;
      return this;
    }
    
    public Builder withPrimaryKey(String primaryKey) {
      this.primaryKey=primaryKey;
      return this;
    }
    
    public Builder forOrigin(String origin) {
      this.origin=origin;
      return this;
    }
    
    public SubscriptionAuditMessage build() {
      return new SubscriptionAuditMessage(this);
    }
    
  }

}
