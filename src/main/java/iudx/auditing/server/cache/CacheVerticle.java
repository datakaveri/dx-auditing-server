package iudx.auditing.server.cache;

import static iudx.auditing.server.common.Constants.CACHE_SERVICE_ADDRESS;
import static iudx.auditing.server.common.Constants.PG_SERVICE_ADDRESS;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.eventbus.MessageConsumer;
import io.vertx.core.json.JsonObject;
import io.vertx.serviceproxy.ServiceBinder;
import iudx.auditing.server.postgres.PostgresService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class CacheVerticle extends AbstractVerticle {

  private static final Logger LOGGER = LogManager.getLogger(CacheVerticle.class);

  private MessageConsumer<JsonObject> consumer;
  private ServiceBinder binder;

  private CacheService cacheService;
  private PostgresService pgService;


  @Override
  public void start() throws Exception {

    pgService = PostgresService.createProxy(vertx, PG_SERVICE_ADDRESS);
    binder = new ServiceBinder(vertx);
    cacheService = new CacheServiceImpl(vertx, pgService);
    consumer = binder.setAddress(CACHE_SERVICE_ADDRESS).register(CacheService.class, cacheService);

    LOGGER.info("Cache Verticle deployed.");
  }

  @Override
  public void stop() {
    binder.unregister(consumer);
  }
}
