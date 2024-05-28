package iudx.auditing.server.cache;

import static iudx.auditing.server.querystrategy.util.Constants.CACHE_QUERY;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import iudx.auditing.server.postgres.PostgresService;
import iudx.auditing.server.processor.subscription.SubscriptionUser;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class CacheServiceImpl implements CacheService {
  private static final Logger LOGGER = LogManager.getLogger(CacheServiceImpl.class);
  private final Cache<String, List<SubscriptionUser>> cache =
      CacheBuilder.newBuilder().maximumSize(5000).expireAfterWrite(1L, TimeUnit.DAYS).build();

  private PostgresService pgService;

  public CacheServiceImpl(Vertx vertx, PostgresService pgService) {
    this.pgService = pgService;

    refreshCache();

    vertx.setPeriodic(
        TimeUnit.DAYS.toMillis(1),
        handler -> {
          refreshCache();
        });
  }

  public static <T> List<T> listOf(JsonArray arr) {
    if (arr == null) {
      return null;
    } else {
      return (List<T>) arr.getList();
    }
  }

  @Override
  public Future<JsonObject> get(String key) {
    LOGGER.info("request for id : {}", key);
    Promise<JsonObject> promise = Promise.promise();
    if (cache.getIfPresent(key) != null) {
      List<SubscriptionUser> subscriptionUsers = cache.getIfPresent(key);
      JsonArray jsonArray = new JsonArray();
      subscriptionUsers.forEach(
          user -> {
            JsonObject jsonObject =
                new JsonObject()
                    .put("user_id", user.getUserId())
                    .put("queue_name", user.getSubsId())
                    .put("entity", user.getResourceId())
                    .put("provider_id", user.getProviderId())
                    .put("resource_group", user.getResourceGroup())
                    .put("provider_id", user.getProviderId())
                    .put("delegator_id", user.getDelegatorId())
                    .put("item_type", user.getType());
            jsonArray.add(jsonObject);
          });
      JsonObject resultJson = new JsonObject().put("results", jsonArray);
      promise.complete(resultJson);

    } else {
      refreshCache()
          .onSuccess(
              successHandler -> {
                if (cache.getIfPresent(key) != null) {
                  List<SubscriptionUser> subscriptionUsers = cache.getIfPresent(key);
                  JsonArray jsonArray = new JsonArray(subscriptionUsers);
                  JsonObject resultJson = new JsonObject().put("results", jsonArray);
                  promise.complete(resultJson);
                } else {
                  LOGGER.info("Subscriber :{} : not found in cache/database server", key);
                  promise.fail("Subscriber doesn't exist/expired");
                }
              })
          .onFailure(
              failureHandler -> {
                promise.fail("Subscriber doesn't exist/expired");
              });
    }
    return promise.future();
  }

  @Override
  public Future<Void> refreshCache() {
    LOGGER.trace("refreshCache() called");
    Promise<Void> promise = Promise.promise();
    pgService
        .executeReadQuery(CACHE_QUERY)
        .onSuccess(
            result -> {
              cache.invalidateAll();
              result.forEach(
                  json -> {
                    JsonObject jsonObject = new JsonObject(json.toString());
                    SubscriptionUser subConsumer = jsonObject.mapTo(SubscriptionUser.class);
                    String resourceId = subConsumer.getResourceId();
                    if (cache.getIfPresent(resourceId) == null) {
                      List<SubscriptionUser> subscriptionUsers = new ArrayList<>();
                      subscriptionUsers.add(subConsumer);
                      cache.put(resourceId, subscriptionUsers);
                    } else {
                      List<SubscriptionUser> subscriptionUsers = cache.getIfPresent(resourceId);
                      subscriptionUsers.add(subConsumer);
                    }
                  });
              promise.complete();
            })
        .onFailure(
            failure -> {
              LOGGER.error("Fail to fetch database : {} ", failure.getMessage());
              promise.fail(failure.getMessage());
            });
    return promise.future();
  }
}
