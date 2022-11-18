package iudx.auditing.server.immudb;

import static iudx.auditing.server.common.Constants.IMMUDB_SERVICE_ADDRESS;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.eventbus.MessageConsumer;
import io.vertx.core.json.JsonObject;
import io.vertx.pgclient.PgConnectOptions;
import io.vertx.pgclient.PgPool;
import io.vertx.serviceproxy.ServiceBinder;
import io.vertx.sqlclient.PoolOptions;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ImmudbVerticle extends AbstractVerticle {
  private static final Logger LOGGER = LogManager.getLogger(ImmudbVerticle.class);
  PgConnectOptions connectOptions;
  PoolOptions poolOptions;
  PgPool pool;
  private String databaseIP;
  private int databasePort;
  private String databaseName;
  private String databaseTableName;
  private String databaseUserName;
  private String databasePassword;
  private int poolSize;
  private ServiceBinder binder;
  private MessageConsumer<JsonObject> consumer;
  private ImmudbService immuDbService;

  @Override
  public void start() throws Exception {

    databaseIP = config().getString("meteringDatabaseIP");
    databasePort = config().getInteger("meteringDatabasePort");
    databaseName = config().getString("meteringDatabaseName");
    databaseUserName = config().getString("meteringDatabaseUserName");
    databasePassword = config().getString("meteringDatabasePassword");
    databaseTableName = config().getString("meteringDatabaseTableName");
    poolSize = config().getInteger("meteringPoolSize");

    this.connectOptions =
        new PgConnectOptions()
            .setPort(databasePort)
            .setHost(databaseIP)
            .setDatabase(databaseName)
            .setUser(databaseUserName)
            .setPassword(databasePassword)
            .setReconnectAttempts(2)
            .setReconnectInterval(1000);

    this.poolOptions = new PoolOptions().setMaxSize(poolSize);
    this.pool = PgPool.pool(vertx, connectOptions, poolOptions);

    binder = new ServiceBinder(vertx);
    immuDbService = new ImmudbServiceImpl(pool);
    consumer =
        binder.setAddress(IMMUDB_SERVICE_ADDRESS).register(ImmudbService.class, immuDbService);
    LOGGER.info("ImmudbVerticle Verticle Started");
  }

  @Override
  public void stop() {
    binder.unregister(consumer);
  }
}
