package iudx.auditing.server.processor.subscription;

import static iudx.auditing.server.common.Constants.DELIVERY_TAG;
import static iudx.auditing.server.querystrategy.ServerOrigin.RS_SERVER;

import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import iudx.auditing.server.cache.CacheService;
import iudx.auditing.server.rabbitmq.RabbitMqService;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.UUID;
import java.util.function.Supplier;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class SubscriptionAuditServiceImpl implements SubscriptionAuditService {
  private static final Logger LOGGER = LogManager.getLogger(SubscriptionAuditServiceImpl.class);

  Supplier<Long> epochSupplier = () -> LocalDateTime.now().toEpochSecond(ZoneOffset.UTC);
  Supplier<String> isoTimeSupplier =
      () -> ZonedDateTime.now(ZoneId.of(ZoneId.SHORT_IDS.get("IST"))).toString();
  Supplier<String> primaryKeySuppler = () -> UUID.randomUUID().toString().replace("-", "");
  Vertx vertx = Vertx.vertx();


  RabbitMqService rabbitMqService;
  CacheService cacheService;


  public SubscriptionAuditServiceImpl(RabbitMqService rabbitMqService, CacheService cacheService) {
    this.rabbitMqService = rabbitMqService;
    this.cacheService = cacheService;
  }

  @Override
  public Future<Void> generateAuditLog(String resourceid, JsonObject consumedMessage,
                                       CacheService cache) {
    Promise<Void> promise = Promise.promise();
    synchronized (this) {
      JsonObject message = consumedMessage.copy();
      message.remove(DELIVERY_TAG);
      long size = message.toString().getBytes().length;
      LOGGER.debug("Json_size:{} ", size);
      cache.get(resourceid).onSuccess(
              cacheHandler -> {
                JsonArray result = cacheHandler.getJsonArray("results");
                result.forEach(
                    object -> {
                      JsonObject json = new JsonObject(object.toString());
                      SubscriptionUser subscriptionUser = json.mapTo(SubscriptionUser.class);
                      SubscriptionAuditMessage auditMessage = new SubscriptionAuditMessage.Builder()
                          .atEpoch(epochSupplier.get())
                          .atIsoTime(isoTimeSupplier.get())
                          .forOrigin(RS_SERVER.getOriginRole())
                          .forResourceId(resourceid)
                          .forUserId(subscriptionUser.getUserId())
                          .withPrimaryKey(primaryKeySuppler.get())
                          .withProviderId(subscriptionUser.getProviderId())
                          .withResponseSize(size)
                          .forResourceGroup(subscriptionUser.getResourceGroup())
                          .forType(subscriptionUser.getType())
                          .withDelegatorId(subscriptionUser.getDelegatorId())
                          .build();
                      publishAuditLogMessage(auditMessage, rabbitMqService);
                    });
                promise.complete();
              })
          .onFailure(f -> {
            LOGGER.error("failed:: " + f.getMessage());
            promise.fail(f.getMessage());
          });
    }
    return promise.future();
  }

  private void publishAuditLogMessage(SubscriptionAuditMessage auditMessage,
                                      RabbitMqService rabbitMqService) {
    rabbitMqService.publishMessage(auditMessage.toJson());

  }
}
