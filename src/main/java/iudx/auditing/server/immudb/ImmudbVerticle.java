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
  PgConnectOptions connectOptionsForRS;
  PgConnectOptions connectOptionsForAAA;
  PgConnectOptions connectOptionsForCAT;
  PoolOptions poolOptions;
  PgPool poolForRS;
  PgPool poolForAAA;
  PgPool poolForCAT;
  private String databaseIP;
  private int databasePort;
  private String databaseNameRS;

  private String databaseUserNameRS;
  private String databasePasswordRS;

  private String databaseNameAAA;

  private String databaseUserNameAAA;
  private String databasePasswordAAA;

  private String databaseNameCAT;

  private String databaseUserNameCAT;
  private String databasePasswordCAT;

  private int poolSize;
  private ServiceBinder binder;
  private MessageConsumer<JsonObject> consumer;
  private ImmudbService immuDbService;

  @Override
  public void start() throws Exception {

    databaseIP = config().getString("meteringDatabaseIP");
    databasePort = config().getInteger("meteringDatabasePort");
    poolSize = config().getInteger("meteringPoolSize");

    databaseNameRS = config().getString("meteringRSDatabaseName");
    databaseUserNameRS = config().getString("meteringRSDatabaseUserName");
    databasePasswordRS = config().getString("meteringRSDatabasePassword");

    databaseNameAAA = config().getString("meteringAAADatabaseName");
    databaseUserNameAAA = config().getString("meteringAAADatabaseUserName");
    databasePasswordAAA = config().getString("meteringAAADatabasePassword");

    databaseNameCAT = config().getString("meteringCATDatabaseName");
    databaseUserNameCAT = config().getString("meteringCATDatabaseUserName");
    databasePasswordCAT = config().getString("meteringCATDatabasePassword");

    this.connectOptionsForRS =
        new PgConnectOptions()
            .setPort(databasePort)
            .setHost(databaseIP)
            .setDatabase(databaseNameRS)
            .setUser(databaseUserNameRS)
            .setPassword(databasePasswordRS)
            .setReconnectAttempts(2)
            .setReconnectInterval(1000);

    this.connectOptionsForAAA =
        new PgConnectOptions()
            .setPort(databasePort)
            .setHost(databaseIP)
            .setDatabase(databaseNameAAA)
            .setUser(databaseUserNameAAA)
            .setPassword(databasePasswordAAA)
            .setReconnectAttempts(2)
            .setReconnectInterval(1000);

    this.connectOptionsForCAT =
        new PgConnectOptions()
            .setPort(databasePort)
            .setHost(databaseIP)
            .setDatabase(databaseNameCAT)
            .setUser(databaseUserNameCAT)
            .setPassword(databasePasswordCAT)
            .setReconnectAttempts(2)
            .setReconnectInterval(1000);

    this.poolOptions = new PoolOptions().setMaxSize(poolSize);
    this.poolForRS = PgPool.pool(vertx, connectOptionsForRS, poolOptions);
    this.poolForAAA = PgPool.pool(vertx, connectOptionsForAAA, poolOptions);
    this.poolForCAT = PgPool.pool(vertx, connectOptionsForCAT, poolOptions);
    binder = new ServiceBinder(vertx);
    immuDbService = new ImmudbServiceImpl(poolForRS, poolForAAA, poolForCAT);
    consumer =
        binder.setAddress(IMMUDB_SERVICE_ADDRESS).register(ImmudbService.class, immuDbService);
    LOGGER.info("ImmudbVerticle Verticle Started");
  }

  @Override
  public void stop() {
    binder.unregister(consumer);
  }
}
