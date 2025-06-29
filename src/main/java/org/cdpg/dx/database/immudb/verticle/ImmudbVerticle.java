package org.cdpg.dx.database.immudb.verticle;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.eventbus.MessageConsumer;
import io.vertx.core.json.JsonObject;
import io.vertx.pgclient.PgConnectOptions;
import io.vertx.serviceproxy.ServiceBinder;
import io.vertx.sqlclient.Pool;
import io.vertx.sqlclient.PoolOptions;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cdpg.dx.database.immudb.service.ImmudbService;
import org.cdpg.dx.database.immudb.service.ImmudbServiceImpl;

import static org.cdpg.dx.common.config.ServiceProxyAddressConstants.IMMUDB_SERVICE_ADDRESS;

public class ImmudbVerticle extends AbstractVerticle {
  private static final Logger LOGGER = LogManager.getLogger(ImmudbVerticle.class);
  PgConnectOptions connectOptions;
  PoolOptions poolOptions;
  Pool pool;

  private String databaseIp;
  private int databasePort;
  private String databaseName;

  private String databaseUserName;
  private String databasePassword;
  private int poolSize;
  private ServiceBinder binder;
  private MessageConsumer<JsonObject> consumer;
  private ImmudbService immuDbService;

  @Override
  public void start() throws Exception {

    databaseIp = config().getString("meteringDatabaseIP");
    databasePort = config().getInteger("meteringDatabasePort");
    poolSize = config().getInteger("meteringPoolSize");
    databaseName = config().getString("meteringDatabaseName");
    databaseUserName = config().getString("meteringDatabaseUserName");
    databasePassword = config().getString("meteringDatabasePassword");

    this.connectOptions =
        new PgConnectOptions()
            .setPort(databasePort)
            .setHost(databaseIp)
            .setDatabase(databaseName)
            .setUser(databaseUserName)
            .setPassword(databasePassword)
            .setReconnectAttempts(2)
            .setReconnectInterval(1000);

    this.poolOptions = new PoolOptions().setMaxSize(poolSize);
    this.pool = Pool.pool(vertx, connectOptions, poolOptions);
    binder = new ServiceBinder(vertx);
    immuDbService = new ImmudbServiceImpl(vertx, pool, connectOptions, poolOptions);
    consumer =
        binder.setAddress(IMMUDB_SERVICE_ADDRESS).register(ImmudbService.class, immuDbService);
    LOGGER.info("ImmudbVerticle Verticle Started");
  }

  @Override
  public void stop() {
    binder.unregister(consumer);
  }
}
