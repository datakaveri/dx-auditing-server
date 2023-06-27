package iudx.auditing.server.immudb;

import static iudx.auditing.server.common.Constants.*;

import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.json.JsonObject;
import io.vertx.pgclient.PgPool;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ImmudbServiceImpl implements ImmudbService {
  private static final Logger LOGGER = LogManager.getLogger(ImmudbServiceImpl.class);
  private final PgPool pgClientForRs;
  private final PgPool pgClientForAaa;
  private final PgPool pgClientForCat;

  public ImmudbServiceImpl(PgPool pgClientForRs, PgPool pgClientForAaa, PgPool pgClientForCat) {
    this.pgClientForRs = pgClientForRs;
    this.pgClientForAaa = pgClientForAaa;
    this.pgClientForCat = pgClientForCat;
  }

  @Override
  public Future<JsonObject> executeWriteQuery(JsonObject query) {
    Promise<JsonObject> promise = Promise.promise();
    if (query.getString(IMMUDB_WRITE_QUERY) != null
        && !query.getString(IMMUDB_WRITE_QUERY).isEmpty()) {
      JsonObject response = new JsonObject();
      PgPool pgClient = poolForOrigin(query.getString(ORIGIN));
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
    } else {
      LOGGER.error("Could not execute write query as the query supplied is blank or null");
      promise.fail("Could not execute write query as the query supplied is blank or null");
    }
    return promise.future();
  }

  private PgPool poolForOrigin(String originServer) {
    switch (originServer) {
      case RS_SERVER:
      case DI_SERVER:
      case GIS_SERVER:
      case FILE_SERVER:
        return pgClientForRs;
      case AAA_SERVER:
        return pgClientForAaa;
      case CAT_SERVER:
        return pgClientForCat;
      default:
        throw new IllegalArgumentException(originServer + "serverOrigin is not defined");
    }
  }
}
