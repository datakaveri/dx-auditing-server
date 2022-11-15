package iudx.auditing.server.immuDb;

import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ImmuDbServiceImpl implements ImmuDbService {
    private static final Logger LOGGER = LogManager.getLogger(ImmuDbServiceImpl.class);
    private String databaseIP;
    private int databasePort;
    private String databaseName;
    private String databaseUserName;
    private String databasePassword;
    private int databasePoolSize;
    private String databaseTableName;
    PgConnectOptions connectOptions;
    PoolOptions poolOptions;
    PgPool pool;
    private final Vertx vertx;

    public ImmuDbServiceImpl(JsonObject propObj, Vertx vertxInstance) {
        if (propObj != null && !propObj.isEmpty()) {
            databaseIP = propObj.getString("meteringDatabaseIP");
            databasePort = propObj.getInteger("meteringDatabasePort");
            databaseName = propObj.getString("meteringDatabaseName");
            databaseUserName = propObj.getString("meteringDatabaseUserName");
            databasePassword = propObj.getString("meteringDatabasePassword");
            databasePoolSize = propObj.getInteger("meteringPoolSize");
            databaseTableName = propObj.getString("meteringDatabaseTableName");
        }

        this.connectOptions =
                new PgConnectOptions()
                        .setPort(databasePort)
                        .setHost(databaseIP)
                        .setDatabase(databaseName)
                        .setUser(databaseUserName)
                        .setPassword(databasePassword)
                        .setReconnectAttempts(2)
                        .setReconnectInterval(1000);

        this.poolOptions = new PoolOptions().setMaxSize(databasePoolSize);
        this.pool = PgPool.pool(vertxInstance, connectOptions, poolOptions);
        this.vertx = vertxInstance;

    }

    @Override
    public Future<JsonObject> executeQuery(String query) {
        return null;
    }
}
