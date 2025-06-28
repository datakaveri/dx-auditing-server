package org.cdpg.dx.auditingserver.health.service.impl;

import io.vertx.core.CompositeFuture;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cdpg.dx.auditingserver.apiserver.ApiServerVerticle;
import org.cdpg.dx.auditingserver.health.service.HealthService;
import org.cdpg.dx.database.postgres.service.PostgresService;
import org.cdpg.dx.databroker.service.DataBrokerService;

public class HealthServiceImpl implements HealthService {
  private static final Logger LOGGER = LogManager.getLogger(ApiServerVerticle.class);

  private final Vertx vertx;
  private final PostgresService postgresService;
  private final DataBrokerService dataBrokerService;
  private final long eventLoopDelayThresholdMs =
      500; // Threshold for event loop delay in milliseconds

  public HealthServiceImpl(
      Vertx vertx, PostgresService postgresService, DataBrokerService dataBrokerService) {
    this.vertx = vertx;
    this.postgresService = postgresService;
    this.dataBrokerService = dataBrokerService;
  }

  @Override
  public Future<Void> checkLiveness() {
    Promise<Void> promise = Promise.promise();
    long startTime = System.nanoTime();

    vertx.runOnContext(
        v -> {
          long duration = System.nanoTime() - startTime;
          long delayMs = duration / 1_000_000;

          if (delayMs > eventLoopDelayThresholdMs) {
            String msg = "Event loop delay too high: " + delayMs + " ms";
            LOGGER.warn(msg);
            promise.fail(msg);
          } else {
            promise.complete();
          }
        });

    return promise.future();
  }

  @Override
  public Future<Void> checkReadiness() {

    // todo: Implement readiness check logic need to check if the database and message broker are connected.
    //  for that need to implement ping methods in PostgresService and DataBrokerService.

   /* Future<Void> pgCheck = postgresService.ping().mapEmpty();
    Future<Void> rmqCheck =
        dataBrokerService.isConnected()
            ? Future.succeededFuture()
            : Future.failedFuture("RabbitMQ is not connected");

    return CompositeFuture.all(pgCheck, rmqCheck).mapEmpty();*/
    return null;
  }
}
