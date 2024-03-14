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
  PgConnectOptions connectOptionsForRs;
  PgConnectOptions connectOptionsForAaa;
  PgConnectOptions connectOptionsForCat;
  PgConnectOptions connectOptionsForOgc;
  PoolOptions poolOptions;
  PgPool poolForRs;
  PgPool poolForAaa;
  PgPool poolForCat;
  PgPool poolForOgc;
  private String databaseIp;
  private int databasePort;
  private String databaseNameRs;

  private String databaseUserNameRs;
  private String databasePasswordRs;

  private String databaseNameAaa;
  private String databaseUserNameAaa;
  private String databasePasswordAaa;
  private String databaseNameCat;
  private String databaseUserNameCat;
  private String databasePasswordCat;
  private String databasePasswordOgc;
  private String databaseNameOgc;
  private String databaseUserNameOgc;

  private int poolSize;
  private ServiceBinder binder;
  private MessageConsumer<JsonObject> consumer;
  private ImmudbService immuDbService;

  @Override
  public void start() throws Exception {

    databaseIp = config().getString("meteringDatabaseIP");
    databasePort = config().getInteger("meteringDatabasePort");
    poolSize = config().getInteger("meteringPoolSize");

    databaseNameRs = config().getString("meteringRSDatabaseName");
    databaseUserNameRs = config().getString("meteringRSDatabaseUserName");
    databasePasswordRs = config().getString("meteringRSDatabasePassword");

    databaseNameAaa = config().getString("meteringAAADatabaseName");
    databaseUserNameAaa = config().getString("meteringAAADatabaseUserName");
    databasePasswordAaa = config().getString("meteringAAADatabasePassword");

    databaseNameCat = config().getString("meteringCATDatabaseName");
    databaseUserNameCat = config().getString("meteringCATDatabaseUserName");
    databasePasswordCat = config().getString("meteringCATDatabasePassword");

    databaseNameOgc = config().getString("meteringOgcDatabaseName");
    databaseUserNameOgc = config().getString("meteringOgcDatabaseUserName");
    databasePasswordOgc = config().getString("meteringOgcDatabasePassword");

    this.connectOptionsForRs =
        new PgConnectOptions()
            .setPort(databasePort)
            .setHost(databaseIp)
            .setDatabase(databaseNameRs)
            .setUser(databaseUserNameRs)
            .setPassword(databasePasswordRs)
            .setReconnectAttempts(2)
            .setReconnectInterval(1000);

    this.connectOptionsForAaa =
        new PgConnectOptions()
            .setPort(databasePort)
            .setHost(databaseIp)
            .setDatabase(databaseNameAaa)
            .setUser(databaseUserNameAaa)
            .setPassword(databasePasswordAaa)
            .setReconnectAttempts(2)
            .setReconnectInterval(1000);

    this.connectOptionsForCat =
        new PgConnectOptions()
            .setPort(databasePort)
            .setHost(databaseIp)
            .setDatabase(databaseNameCat)
            .setUser(databaseUserNameCat)
            .setPassword(databasePasswordCat)
            .setReconnectAttempts(2)
            .setReconnectInterval(1000);

    this.connectOptionsForOgc =
        new PgConnectOptions()
            .setPort(databasePort)
            .setHost(databaseIp)
            .setDatabase(databaseNameOgc)
            .setUser(databaseUserNameOgc)
            .setPassword(databasePasswordOgc)
            .setReconnectAttempts(2)
            .setReconnectInterval(1000);

    this.poolOptions = new PoolOptions().setMaxSize(poolSize);
    this.poolForRs = PgPool.pool(vertx, connectOptionsForRs, poolOptions);
    this.poolForAaa = PgPool.pool(vertx, connectOptionsForAaa, poolOptions);
    this.poolForCat = PgPool.pool(vertx, connectOptionsForCat, poolOptions);
    this.poolForOgc = PgPool.pool(vertx, connectOptionsForOgc, poolOptions);
    binder = new ServiceBinder(vertx);
    immuDbService = new ImmudbServiceImpl(poolForRs, poolForAaa, poolForCat, poolForOgc);
    consumer =
        binder.setAddress(IMMUDB_SERVICE_ADDRESS).register(ImmudbService.class, immuDbService);
    LOGGER.info("ImmudbVerticle Verticle Started");
  }

  @Override
  public void stop() {
    binder.unregister(consumer);
  }
}
