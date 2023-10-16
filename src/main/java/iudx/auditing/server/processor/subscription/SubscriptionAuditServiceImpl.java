package iudx.auditing.server.processor.subscription;

import static iudx.auditing.server.common.Constants.DELIVERY_TAG;
import static iudx.auditing.server.querystrategy.ServerOrigin.RS_SERVER;

import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import iudx.auditing.server.rabbitmq.RabbitMqService;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class SubscriptionAuditServiceImpl implements SubscriptionAuditService {
  private static final Logger LOGGER = LogManager.getLogger(SubscriptionAuditServiceImpl.class);
  Map<String, List<SubscriptionUser>> subscribers;
  Map<String, String> sub2ResourceIdMap;

  Supplier<Long> epochSupplier = () -> LocalDateTime.now().toEpochSecond(ZoneOffset.UTC);
  Supplier<String> isoTimeSupplier =
      () -> ZonedDateTime.now(ZoneId.of(ZoneId.SHORT_IDS.get("IST"))).toString();
  Supplier<String> primaryKeySuppler = () -> UUID.randomUUID().toString().replace("-", "");


  RabbitMqService rabbitMqService;
  Vertx vertx;

  public SubscriptionAuditServiceImpl(RabbitMqService rabbitMqService) {
    this.subscribers = new ConcurrentHashMap<>();
    this.sub2ResourceIdMap = new ConcurrentHashMap<>();
    this.rabbitMqService = rabbitMqService;
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
    String resourceId = sub2ResourceIdMap.get(subsId);
    List<SubscriptionUser> subsList = subscribers.get(resourceId);
    subsList.removeIf(subsUser -> subsUser.getSubsId().equals(subsId));
  }


  @Override
  public SubscriptionUser getSubsConsumer(String subsId) {
    String resourceId = sub2ResourceIdMap.get(subsId);
    List<SubscriptionUser> subsList = subscribers.get(resourceId);
    Optional<SubscriptionUser> subUser =  subsList.stream().filter(subsUser -> subsUser.getSubsId().equals(subsId)).findAny();
    if(subUser.isPresent()){
      return subUser.get();
    }
    return null;
  }


  @Override
  public void generateAuditLog(String resourceid, JsonObject consumedMessage) {
    synchronized (this) {
      JsonObject message = consumedMessage.copy();
      message.remove(DELIVERY_TAG);
      long size = message.toString().getBytes().length;
      LOGGER.debug("Json_size:{} ", size);

      List<SubscriptionUser> allSubscribersForResourceId = subscribers.getOrDefault(resourceid,new ArrayList<>());
      allSubscribersForResourceId.forEach(
          consumer -> {

            SubscriptionAuditMessage auditMessage = new SubscriptionAuditMessage.Builder()
                .atEpoch(epochSupplier.get())
                .atIsoTime(isoTimeSupplier.get())
                .forOrigin(RS_SERVER.getOriginRole())
                .forResourceId(resourceid)
                .forUserId(consumer.getUserId())
                .withPrimaryKey(primaryKeySuppler.get())
                .withProviderId(consumer.getProviderId())
                .withResponseSize(size)
                .forResourceGroup(consumer.getResourceGroup())
                .forType(consumer.getType())
                .withDelegatorId(consumer.getDelegatorId())
                .build();
            consumer.publishAuditLogMessage(auditMessage, rabbitMqService);
          });
    }
  }
}
