package iudx.auditing.server.processor.subscription;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;
import io.vertx.core.json.JsonObject;

public class SubscriptionAuditServiceImpl implements SubscriptionAuditService {

  Map<String, List<SubscriptionUser>> subscribers;
  Map<String,String> sub2ResourceIdMap;
  
  Supplier<Long> epochSupplier=()->LocalDateTime.now().toEpochSecond(ZoneOffset.UTC);
  Supplier<String> isoTimeSupplier=()->ZonedDateTime.now(ZoneId.of(ZoneId.SHORT_IDS.get("IST"))).toString();

  public SubscriptionAuditServiceImpl() {
    this.subscribers = new ConcurrentHashMap<>();
    this.sub2ResourceIdMap=new ConcurrentHashMap<>();
    //To handle failure pre fill maps above from DB on startup/object creations
  }

  @Override
  public void addSubsConsumer(SubscriptionUser subConsumer) {
    String resourceId = subConsumer.getResourceId();
    List<SubscriptionUser> resourceSubscribers =
        subscribers.computeIfAbsent(resourceId, k -> new ArrayList<SubscriptionUser>());
    resourceSubscribers.add(subConsumer);
    sub2ResourceIdMap.put(subConsumer.getSubsId(), resourceId);
  }

  @Override
  public void deleteSubsConsumer(String subsId) {
    String resourceId=sub2ResourceIdMap.get(subsId);
    List<SubscriptionUser> subsList=subscribers.get(resourceId);
    subsList.removeIf(subsUser->subsUser.getSubsId().equals(subsId));
  }

  @Override
  public void generateAuditLog(String resourceid, JsonObject consumedMessage) {
    synchronized (this) {
      //calculate size [look in Java Instrumentation class]
      //get provider for resource_id [through cat API]
      //generate message format
      SubscriptionAuditMessage auditMessage=new SubscriptionAuditMessage.Builder()
          .atEpoch(epochSupplier.get())
          .atIsoTime(isoTimeSupplier.get())
          .forOrigin("rs-server")
          .forResourceId("resource_id")
          .forUserId("user_id")
          .withPrimaryKey("generate pk")
          .withProviderId("getProviderID")
          .withResponseSize(123)//calculate message size
          .build();
      
      List<SubscriptionUser> allSubscribersForResourceId=subscribers.get(resourceid);
      allSubscribersForResourceId.forEach(consumer->consumer.publishAuditLogMessage(auditMessage));
    }
  }
  
  
  


}
