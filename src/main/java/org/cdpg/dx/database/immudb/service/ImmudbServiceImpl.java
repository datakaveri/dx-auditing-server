package org.cdpg.dx.database.immudb.service;

import static org.cdpg.dx.databroker.listeners.util.Constans.IMMUDB_WRITE_QUERY;
import static org.cdpg.dx.databroker.listeners.util.Constans.RESULT;

import org.shaded.immudb4j.sql.SQLValue;
import org.shaded.immudb4j.ImmuClient;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.json.JsonObject;
import io.vertx.sqlclient.Pool;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ImmudbServiceImpl implements ImmudbService {
  private static final Logger LOGGER = LogManager.getLogger(ImmudbServiceImpl.class);
  private Pool pool;
  private ImmuClient client;

  public ImmudbServiceImpl(Pool pool) {
    this.pool = pool;
  }

  public ImmudbServiceImpl(ImmuClient client) {
    this.client = client;
  }

  public ImmudbServiceImpl() {
  }

  @Override
  public Future<JsonObject> executeWriteQueryUsingClient() {
    Promise<JsonObject> promise = Promise.promise();
//    if (query.getString(IMMUDB_WRITE_QUERY) != null
//        && !query.getString(IMMUDB_WRITE_QUERY).isEmpty()) {
//      JsonObject response = new JsonObject();

      client.openSession("dummy-db", "dummy-user", "dummy-password");
      String queryString =
          "INSERT INTO auditing_acl_apd (id, userid, endpoint, method, body, size, isotime, epochtime) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";


      try {
        client.sqlExec(queryString,
            new SQLValue("dummy-id-1"),
            new SQLValue("user-123"),
            new SQLValue("/api/test"),
            new SQLValue("POST"),
            new SQLValue("{\"key\":\"value\"}"),
            new SQLValue(123),
            new SQLValue("2024-06-01T12:00:00Z"),
            new SQLValue(1717233600)
        );
        System.out.println("SQL execution succeeded");
        promise.complete(new JsonObject().put(RESULT, "Table Updated Successfully"));
      } catch (Exception e) {
        e.printStackTrace();
        System.err.println("SQL execution failed: " + e.getMessage());
        promise.fail(e);
      } finally {
        if (client != null) {
          client.closeSession();
        }
      }


//      pool
//          .withConnection(
//              connection -> connection.query(query.getString(IMMUDB_WRITE_QUERY)).execute())
//          .onComplete(
//              rows -> {
//                if (rows.succeeded()) {
//                  LOGGER.debug("Immudb Table updated successfully");
//                  response.put(RESULT, "Table Updated Successfully");
//                  promise.complete(response);
//                } else {
//                  LOGGER.error("Info: failed :" + rows.cause());
//                  response.put(RESULT, rows.cause());
//                  promise.fail(rows.cause());
//                }
//              });
//    } else {
//      LOGGER.error("Could not execute write query as the query supplied is blank or null");
//      promise.fail("Could not execute write query as the query supplied is blank or null");
//    }
    return promise.future();
  }


  @Override
  public Future<JsonObject> executeWriteQuery(JsonObject query) {
    Promise<JsonObject> promise = Promise.promise();
    if (query.getString(IMMUDB_WRITE_QUERY) != null
        && !query.getString(IMMUDB_WRITE_QUERY).isEmpty()) {
      JsonObject response = new JsonObject();
//      PgPool pgClient = poolForOrigin(query.getString(ORIGIN));
      pool
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

//  private PgPool poolForOrigin(String originServer) {
//    switch (originServer) {
//      case RS_SERVER:
//      case DI_SERVER:
//      case GIS_SERVER:
//      case FILE_SERVER:
//      case ACL_APD_SERVER:
//      case DMP_APD_SERVER:
//      case CONSENT_LOG_ADEX:
//      case OGC_SERVER:
//      case AAA_SERVER:
//      case CAT_SERVER:
//        return pool;
//      default:
//        throw new IllegalArgumentException(originServer + " serverOrigin is not defined");
//    }
//  }
}