package iudx.auditing.server.processor;

import static iudx.auditing.server.common.Constants.CACHE_SERVICE_ADDRESS;
import static iudx.auditing.server.common.Constants.IMMUDB_SERVICE_ADDRESS;
import static iudx.auditing.server.common.Constants.MSG_PROCESS_ADDRESS;
import static iudx.auditing.server.common.Constants.PG_SERVICE_ADDRESS;
import static iudx.auditing.server.common.Constants.RMQ_SERVICE_ADDRESS;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.eventbus.MessageConsumer;
import io.vertx.core.json.JsonObject;
import io.vertx.serviceproxy.ServiceBinder;
import iudx.auditing.server.cache.CacheService;
import iudx.auditing.server.immudb.ImmudbService;
import iudx.auditing.server.postgres.PostgresService;
import iudx.auditing.server.processor.subscription.SubscriptionAuditServiceImpl;
import iudx.auditing.server.rabbitmq.RabbitMqService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ProcessorVerticle extends AbstractVerticle {
  private static final Logger LOGGER = LogManager.getLogger(ProcessorVerticle.class);

  private MessageProcessService processor;
  private PostgresService postgresService;
  private ImmudbService immudbService;
  private ServiceBinder binder;
  private MessageConsumer<JsonObject> consumer;
  private RabbitMqService rabbitMqService;
  private CacheService cacheService;
  private SubscriptionAuditServiceImpl subscriptionAuditService;

  @Override
  public void start() throws Exception {
    postgresService = PostgresService.createProxy(vertx, PG_SERVICE_ADDRESS);
    immudbService = ImmudbService.createProxy(vertx, IMMUDB_SERVICE_ADDRESS);
    rabbitMqService = RabbitMqService.createProxy(vertx, RMQ_SERVICE_ADDRESS);
    cacheService = CacheService.createProxy(vertx, CACHE_SERVICE_ADDRESS);
    subscriptionAuditService = new SubscriptionAuditServiceImpl(rabbitMqService, cacheService);
    processor =
        new MessageProcessorImpl(
            postgresService, immudbService, subscriptionAuditService, config(), cacheService);
    binder = new ServiceBinder(vertx);

    consumer =
        binder.setAddress(MSG_PROCESS_ADDRESS).register(MessageProcessService.class, processor);
    LOGGER.info("Processor Verticle deployed.");
  }

  @Override
  public void stop() {
    binder.unregister(consumer);
  }
}
