package org.cdpg.dx.database.immudb.service;

import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.pgclient.PgConnectOptions;
import io.vertx.sqlclient.Pool;
import io.vertx.sqlclient.PoolOptions;
import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.RowSet;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cdpg.dx.database.immudb.query.InsertQuery;

public class ImmudbServiceImpl implements ImmudbService {
  private static final Logger LOGGER = LogManager.getLogger(ImmudbServiceImpl.class);
  private static final int MAX_RETRIES = 2;
  private static final int VERIFICATION_TIMEOUT_MS = 2000;

  private final Vertx vertx;
  private final PgConnectOptions connectOptions;
  private final PoolOptions poolOptions;
  private Pool pool;

  public ImmudbServiceImpl(
      Vertx vertx, Pool pool, PgConnectOptions connectOptions, PoolOptions poolOptions) {
    this.vertx = vertx;
    this.pool = pool;
    this.connectOptions = connectOptions;
    this.poolOptions = poolOptions;
  }

  @Override
  public Future<Boolean> executeQuery(InsertQuery insertQuery, String verificationFild) {
    Promise<Boolean> promise = Promise.promise();
    attemptQuery(insertQuery, verificationFild, promise, MAX_RETRIES);
    return promise.future();
  }

  private void attemptQuery(
      InsertQuery insertQuery, String verificationFild, Promise<Boolean> promise, int retriesLeft) {
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
                verifyInsertWithTimeout(insertQuery, verificationFild)
                    .onComplete(
                        verifyResult -> {
                          if (verifyResult.succeeded() && verifyResult.result()) {
                            LOGGER.info(
                                "Insert verification succeeded - data was actually inserted");
                            promise.complete(true);
                          } else {
                            LOGGER.warn("Insert verification failed or timed out");
                            handleRetryOrPoolReset(
                                insertQuery, verificationFild, promise, retriesLeft);
                          }
                        });
              }
            });
  }

  private Future<Boolean> verifyInsertWithTimeout(
      InsertQuery insertQuery, String verificationFild) {
    Promise<Boolean> promise = Promise.promise();

    // Set up a timeout
    long timerId =
        vertx.setTimer(
            VERIFICATION_TIMEOUT_MS,
            id -> {
              if (!promise.future().isComplete()) {
                LOGGER.warn("Verification timed out after {} ms", VERIFICATION_TIMEOUT_MS);
                promise.tryFail("Verification timed out");
              }
            });

    verifyInsert(insertQuery, verificationFild)
        .onComplete(
            result -> {
              vertx.cancelTimer(timerId);
              if (!promise.future().isComplete()) {
                if (result.succeeded()) {
                  promise.tryComplete(result.result());
                } else {
                  promise.tryFail(result.cause());
                }
              }
            });

    return promise.future();
  }

  private Future<Boolean> verifyInsert(InsertQuery insertQuery, String verificationFild) {
    Promise<Boolean> promise = Promise.promise();

    try {
      String verificationQuery = buildVerificationQuery(insertQuery.getTable(), verificationFild);
      if (verificationQuery == null) {
        LOGGER.warn("Cannot verify insert - no verification query could be built");
        promise.complete(false);
        return promise.future();
      }

      LOGGER.debug("Executing verification query: {}", verificationQuery);
      pool.query(verificationQuery)
          .execute()
          .onComplete(
              result -> {
                if (result.succeeded()) {
                  boolean exists = checkIfDataExists(result.result());
                  LOGGER.debug("Verification result: {}", exists);
                  promise.complete(exists);
                } else {
                  LOGGER.warn("Verification query failed: {}", result.cause().getMessage());
                  promise.complete(false);
                }
              });
    } catch (Exception e) {
      LOGGER.error("Error during verification: {}", e.getMessage());
      promise.complete(false);
    }

    return promise.future();
  }

  private String buildVerificationQuery(String tableName, String verificatioFild) {

    String query = "SELECT * FROM " + tableName + " WHERE id = '" + verificatioFild + "'";
    LOGGER.info("Building verification query for table: {}, field: {}", tableName, verificatioFild);
    LOGGER.info("Verification query: {}", query);
    return query;
  }

  private boolean checkIfDataExists(RowSet<Row> result) {
    return result != null && result.iterator().hasNext();
  }

  private void handleRetryOrPoolReset(
      InsertQuery insertQuery, String verificatioFild, Promise<Boolean> promise, int retriesLeft) {
    if (retriesLeft > 0) {
      LOGGER.info("Retrying... attempts left: {}", retriesLeft);
      vertx.setTimer(
          1000, id -> attemptQuery(insertQuery, verificatioFild, promise, retriesLeft - 1));
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
                              LOGGER.info("Immudb Table updated successfully after pool reset.");
                              promise.complete(true);
                            } else {
                              verifyInsertWithTimeout(insertQuery, verificatioFild)
                                  .onComplete(
                                      lastVerify -> {
                                        if (lastVerify.succeeded() && lastVerify.result()) {
                                          promise.complete(true);
                                        } else {
                                          LOGGER.error(
                                              "Final attempt failed after pool reset: {}",
                                              finalTry.cause().getMessage());
                                          promise.fail(finalTry.cause());
                                        }
                                      });
                            }
                          });
                } else {
                  LOGGER.error("Failed to reset connection pool: {}", ar.cause().getMessage());
                  promise.fail(ar.cause());
                }
              });
    }
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
