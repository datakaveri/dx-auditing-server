package org.cdpg.dx.database.immudb.service;

import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.sqlclient.Pool;
import io.vertx.sqlclient.PoolOptions;
import io.vertx.sqlclient.SqlConnectOptions;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cdpg.dx.database.immudb.query.InsertQuery;

public class ImmudbServiceImpl implements ImmudbService {
  private static final Logger LOGGER = LogManager.getLogger(ImmudbServiceImpl.class);
  private static final int MAX_RETRIES = 2;
  private final Vertx vertx;
  private final SqlConnectOptions connectOptions;
  private final PoolOptions poolOptions;
  private Pool pool;

  public ImmudbServiceImpl(
      Vertx vertx, Pool pool, SqlConnectOptions connectOptions, PoolOptions poolOptions) {
    this.vertx = vertx;
    this.pool = pool;
    this.connectOptions = connectOptions;
    this.poolOptions = poolOptions;
  }

  @Override
  public Future<Boolean> executeQuery(InsertQuery insertQuery) {
    Promise<Boolean> promise = Promise.promise();
    attemptQuery(insertQuery, promise, MAX_RETRIES);
    return promise.future();
  }

  private void attemptQuery(InsertQuery insertQuery, Promise<Boolean> promise, int retriesLeft) {
    LOGGER.debug("Executing Immudb Insert Query: {}", insertQuery.toSQL());

    pool.query(insertQuery.toSQL())
        .execute()
        .onComplete(
            rows -> {
              if (rows.succeeded()) {
                LOGGER.info("Immudb Table updated successfully");
                promise.complete(true);
              } else {
                LOGGER.error("Insert failed: {}", rows.cause().getMessage());
                if (retriesLeft > 0) {
                  LOGGER.info("Retrying... attempts left: {}", retriesLeft);
                  attemptQuery(insertQuery, promise, retriesLeft - 1);
                } else {
                  LOGGER.warn("All retry attempts exhausted. Closing and recreating pool...");
                  resetPool()
                      .onComplete(
                          ar -> {
                            if (ar.succeeded()) {
                              LOGGER.info("Pool reset successfully. Trying final attempt...");
                              pool.query(insertQuery.toSQL())
                                  .execute()
                                  .onComplete(
                                      finalTry -> {
                                        if (finalTry.succeeded()) {
                                          LOGGER.info(
                                              "Immudb Table updated successfully after pool reset.");
                                          promise.complete(true);
                                        } else {
                                          LOGGER.error(
                                              "Final attempt failed after pool reset: {}",
                                              finalTry.cause().getMessage());
                                          promise.fail(finalTry.cause());
                                        }
                                      });
                            } else {
                              LOGGER.error(
                                  "Failed to reset connection pool: {}", ar.cause().getMessage());
                              promise.fail(ar.cause());
                            }
                          });
                }
              }
            });
  }

  private Future<Void> resetPool() {
    Promise<Void> promise = Promise.promise();
    pool.close()
        .onComplete(
            closeAr -> {
              if (closeAr.succeeded()) {
                LOGGER.info("Existing pool closed. Recreating new pool...");
                this.pool = Pool.pool(vertx, connectOptions, poolOptions);
                promise.complete();
              } else {
                LOGGER.error("Error closing the pool: {}", closeAr.cause().getMessage());
                promise.fail(closeAr.cause());
              }
            });
    return promise.future();
  }
}
