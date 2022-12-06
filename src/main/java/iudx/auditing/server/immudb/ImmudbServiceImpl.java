package iudx.auditing.server.immudb;

import static iudx.auditing.server.common.Constants.AAA_SERVER;
import static iudx.auditing.server.common.Constants.CAT_SERVER;
import static iudx.auditing.server.common.Constants.DI_SERVER;
import static iudx.auditing.server.common.Constants.FILE_SERVER;
import static iudx.auditing.server.common.Constants.GIS_SERVER;
import static iudx.auditing.server.common.Constants.IMMUDB_WRITE_QUERY;
import static iudx.auditing.server.common.Constants.ORIGIN;
import static iudx.auditing.server.common.Constants.RESULT;
import static iudx.auditing.server.common.Constants.RS_SERVER;

import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.json.JsonObject;
import io.vertx.pgclient.PgPool;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ImmudbServiceImpl implements ImmudbService {
  private static final Logger LOGGER = LogManager.getLogger(ImmudbServiceImpl.class);
  private final PgPool pgClientForRS;
  private final PgPool pgClientForAAA;
  private final PgPool pgClientForCAT;

  public ImmudbServiceImpl(PgPool pgClientForRS, PgPool pgClientForAAA, PgPool pgClientForCAT) {
    this.pgClientForRS = pgClientForRS;
    this.pgClientForAAA = pgClientForAAA;
    this.pgClientForCAT = pgClientForCAT;
  }

  @Override
  public Future<JsonObject> executeWriteQuery(JsonObject query) {
    Promise<JsonObject> promise = Promise.promise();
    JsonObject response = new JsonObject();
    PgPool pgClient = PoolForOrigin(query.getString(ORIGIN));
    pgClient
        .withConnection(
            connection -> connection.query(query.getString(IMMUDB_WRITE_QUERY)).execute())
        .onComplete(
            rows -> {
              if (rows.succeeded()) {
                LOGGER.debug("Immudb Table updated successfully");
                response.put(RESULT, "Table Updated Successfully");
                promise.complete(response);
              } else {
                LOGGER.error("Info: failed :" + rows.cause());
                response.put(RESULT, rows.cause());
                promise.fail(rows.cause());
              }
            });
    return promise.future();
  }
  private PgPool PoolForOrigin(String originServer) {
    switch (originServer) {
      case RS_SERVER:
      case DI_SERVER:
      case GIS_SERVER:
      case FILE_SERVER:
        return pgClientForRS;
      case AAA_SERVER:
        return pgClientForAAA;
      case CAT_SERVER:
        return pgClientForCAT;
      default:
        throw new IllegalArgumentException(originServer + "serverOrigin is not defined");
    }
  }
}
