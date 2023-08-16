package iudx.auditing.server.processor.subscription;

import static iudx.auditing.server.querystrategy.ServerOrigin.RS_SERVER_SUBS;

import io.vertx.core.Future;
import io.vertx.core.Vertx;
import iudx.auditing.server.processor.subscription.catService.CatalogueService;
import iudx.auditing.server.processor.subscription.catService.CatalogueServiceImpl;
import iudx.auditing.server.rabbitmq.RabbitMqService;
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
  JsonObject config;
  CatalogueService catalogueService;
  RabbitMqService rabbitMqService;
  Vertx vertx;

  public SubscriptionAuditServiceImpl(JsonObject config, RabbitMqService rabbitMqService) {
    this.subscribers = new ConcurrentHashMap<>();
    this.sub2ResourceIdMap=new ConcurrentHashMap<>();
    this.config = config;
    this.rabbitMqService = rabbitMqService;
    this.catalogueService = new CatalogueServiceImpl(vertx
        , config);
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
      Future<JsonObject> catItemFuture = catalogueService.searchCatItem(resourceid);
      catItemFuture.onSuccess(
          catItem -> {
            SubscriptionAuditMessage auditMessage = new SubscriptionAuditMessage.Builder()
                .atEpoch(epochSupplier.get())
                .atIsoTime(isoTimeSupplier.get())
                .forOrigin(RS_SERVER_SUBS.name())//
                .forResourceId(resourceid)
                .forUserId(consumedMessage.getString("userid"))
                .withPrimaryKey("generate pk")
                .withProviderId(catItem.getString("provider"))
                .withResponseSize(123)//calculate message size
                .build();

            List<SubscriptionUser> allSubscribersForResourceId = subscribers.get(resourceid);
            allSubscribersForResourceId.forEach(
                consumer -> consumer.publishAuditLogMessage(auditMessage,rabbitMqService));

          });

    }
  }
}
