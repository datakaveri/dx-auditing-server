package org.cdpg.dx.database.immudb.verticle;

import static org.cdpg.dx.databroker.listeners.util.Constans.IMMUDB_SERVICE_ADDRESS;

import org.shaded.immudb4j.ImmuClient;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.eventbus.MessageConsumer;
import io.vertx.core.json.JsonObject;
import io.vertx.serviceproxy.ServiceBinder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cdpg.dx.database.immudb.service.ImmudbService;
import org.cdpg.dx.database.immudb.service.ImmudbServiceImpl;

public class ImmudbVerticle extends AbstractVerticle {
  private static final Logger LOGGER = LogManager.getLogger(ImmudbVerticle.class);
  private String databaseIp;
  private int databasePort;
  private String databaseName;

  private String databaseUserName;
  private String databasePassword;

  private ImmuClient immuClient;
  private ServiceBinder binder;
  private MessageConsumer<JsonObject> consumer;
  private ImmudbService immuDbService;

  @Override
  public void start() throws Exception {

    databaseIp = config().getString("meteringDatabaseIP");
    databasePort = config().getInteger("meteringDatabasePort");
    databaseName = config().getString("meteringDatabaseName");
    databaseUserName = config().getString("meteringDatabaseUserName");
    databasePassword = config().getString("meteringDatabasePassword");
    LOGGER.info("Attempting to connect to ImmuDB at {}:{}", databaseIp, databasePort);


    try {
      LOGGER.info("Building ImmuClient...");
      immuClient = ImmuClient.newBuilder()
          .withServerUrl(databaseIp)
          .withServerPort(databasePort)
          .withKeepAlivePeriod(15000)
          .build();
//      LOGGER.info("ImmuClient built successfully.");
//      LOGGER.info("Attempting to open session for database: {}", databaseName);
//      immuClient.openSession(databaseName, databaseUserName, databasePassword);
//      LOGGER.info("Session opened successfully.");
//      String serverInfo = immuClient.currentState().toString();
//      LOGGER.info("Connected to ImmuDB server. Server info: {}", serverInfo);

    } catch (Exception e) {
      e.printStackTrace();
      LOGGER.error("Failed to open session with immudb: {}", e.getMessage());
      throw new RuntimeException("Failed to open session with immudb", e);
    }

    binder = new ServiceBinder(vertx);
    immuDbService = new ImmudbServiceImpl(immuClient);
    consumer = binder.setAddress(IMMUDB_SERVICE_ADDRESS).register(ImmudbService.class, immuDbService);
    LOGGER.info("ImmudbVerticle Verticle Started");
  }

  @Override
  public void stop() {
    immuClient.closeSession();
    binder.unregister(consumer);
  }
}
