package org.cdpg.dx.database.immudb.service;

import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.sqlclient.Pool;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cdpg.dx.database.immudb.query.InsertQuery;

public class ImmudbServiceImpl implements ImmudbService {
  private static final Logger LOGGER = LogManager.getLogger(ImmudbServiceImpl.class);
  private final Pool pool;

  public ImmudbServiceImpl(Pool pool) {
    this.pool = pool;
  }


  @Override
  public Future<Boolean> executeQuery(InsertQuery insertQuery) {
    Promise<Boolean> promise = Promise.promise();
    LOGGER.debug("Insert Query : {}", insertQuery.toSQL());
    pool.query(insertQuery.toSQL()).execute()
        .onComplete(
            rows -> {
              if (rows.succeeded()) {
                LOGGER.info("Immudb Table updated successfully");
                promise.complete(true);
              } else {
                LOGGER.error("Failure : {}", rows.cause().getCause().getMessage());
                promise.fail(rows.cause());
              }
            });
    return promise.future();
  }
}

