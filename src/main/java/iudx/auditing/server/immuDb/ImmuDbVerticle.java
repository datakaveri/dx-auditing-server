package iudx.auditing.server.immuDb;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.eventbus.MessageConsumer;
import io.vertx.core.json.JsonObject;
import io.vertx.serviceproxy.ServiceBinder;

import static iudx.auditing.server.common.Constants.IMMUDB_SERVICE_ADDRESS;
import static org.apache.logging.log4j.core.AbstractLifeCycle.LOGGER;

public class ImmuDbVerticle extends AbstractVerticle {
    private static final Logger LOGGER = LogManager.getLogger(ImmuDbVerticle.class);
    private String databaseIP;
    private int databasePort;
    private String databaseName;
    private String databaseTableName;
    private String databaseUserName;
    private String databasePassword;
    private int poolSize;
    private PgConnectOptions config;
    private ServiceBinder binder;
    private MessageConsumer<JsonObject> consumer;
    private ImmuDbService immuDbService;

    @Override
    public void start() throws Exception {

        databaseIP = config().getString("meteringDatabaseIP");
        databasePort = config().getInteger("meteringDatabasePort");
        databaseName = config().getString("meteringDatabaseName");
        databaseUserName = config().getString("meteringDatabaseUserName");
        databasePassword = config().getString("meteringDatabasePassword");
        databaseTableName = config().getString("meteringDatabaseTableName");
        poolSize = config().getInteger("meteringPoolSize");

        JsonObject propObj = new JsonObject();
        propObj.put("meteringDatabaseIP", databaseIP);
        propObj.put("meteringDatabasePort", databasePort);
        propObj.put("meteringDatabaseName", databaseName);
        propObj.put("meteringDatabaseUserName", databaseUserName);
        propObj.put("meteringDatabasePassword", databasePassword);
        propObj.put("meteringPoolSize", poolSize);
        propObj.put("meteringDatabaseTableName", databaseTableName);

        binder = new ServiceBinder(vertx);
        immuDbService = new ImmuDbServiceImpl(propObj, vertx);
        consumer =
                binder.setAddress(IMMUDB_SERVICE_ADDRESS).register(ImmuDbService.class, immuDbService);
        LOGGER.info("ImmuDbVerticle Verticle Started");
    }

    @Override
    public void stop() {
        binder.unregister(consumer);
    }
}
