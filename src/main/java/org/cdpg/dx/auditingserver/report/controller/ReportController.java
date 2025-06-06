package org.cdpg.dx.auditingserver.report.controller;

import io.vertx.core.Handler;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.ext.auth.User;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.openapi.RouterBuilder;
import java.util.UUID;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cdpg.dx.auditingserver.apiserver.ApiController;
import org.cdpg.dx.auditingserver.report.service.ReportService;
import org.cdpg.dx.auth.authorization.handler.AuthorizationHandler;
import org.cdpg.dx.auth.authorization.model.DxRole;

public class ReportController implements ApiController {
  private static final Logger LOGGER = LogManager.getLogger(ReportController.class);
  private final ReportService reportService;

  public ReportController(ReportService reportService) {
    this.reportService = reportService;
  }

  @Override
  public void register(RouterBuilder builder) {

    Handler<RoutingContext> adminAccessHandler =
        AuthorizationHandler.forRoles(DxRole.ORG_ADMIN, DxRole.COS_ADMIN);
    Handler<RoutingContext> consumerAccessHandler = AuthorizationHandler.forRoles(DxRole.CONSUMER);

    builder
        .operation("get-admin-report")
        .handler(adminAccessHandler)
        .handler(this::handleGenerateCsvForAdmin);
    builder
        .operation("get-consumer-report")
        .handler(consumerAccessHandler)
        .handler(this::handleGenerateCsvForConsumer);
  }

  private void handleGenerateCsvForAdmin(RoutingContext routingContext) {
    HttpServerResponse response = routingContext.response();
    response
        .putHeader("Access-Control-Allow-Origin", "*")
        .putHeader("Access-Control-Allow-Headers", "Content-Type, Authorization")
        .putHeader("Access-Control-Allow-Methods", "GET, POST,PUT, DELETE, OPTIONS")
        .putHeader("Content-Type", "text/csv")
        .putHeader("Content-Disposition", "attachment; filename=\"admin_report.csv\"")
        .setChunked(true);

    reportService
        .streamAdminCsvBatched()
        .onSuccess(
            csvStream -> {
              if (csvStream == null) {
                response.end();
                return;
              }
              csvStream.handler(buffer -> response.write(buffer));
              csvStream.endHandler(v -> response.end());
              csvStream.exceptionHandler(
                  err -> {
                    LOGGER.error("Failed to stream CSV", err);
                    routingContext.fail(err);
                  });
            })
        .onFailure(
            err -> {
              LOGGER.error("Failed to stream CSV", err);
              routingContext.fail(err);
            });
  }

  private void handleGenerateCsvForConsumer(RoutingContext routingContext) {
    HttpServerResponse response = routingContext.response();
    response
        .putHeader("Access-Control-Allow-Origin", "*")
        .putHeader("Access-Control-Allow-Headers", "Content-Type, Authorization")
        .putHeader("Access-Control-Allow-Methods", "GET, POST,PUT, DELETE, OPTIONS")
        .putHeader("Content-Type", "text/csv")
        .putHeader("Content-Disposition", "attachment; filename=\"consumer_report.csv\"")
        .setChunked(true);
    User user = routingContext.user();
    UUID userId = UUID.fromString(user.subject());
    reportService
        .streamConsumerCsvBatched(userId)
        .onSuccess(
            csvStream -> {
              if (csvStream == null) {
                response.end();
                return;
              }
              csvStream.handler(buffer -> response.write(buffer));
              csvStream.endHandler(v -> response.end());
              csvStream.exceptionHandler(
                  err -> {
                    LOGGER.error("Failed to stream CSV", err);
                    routingContext.fail(err);
                  });
            })
        .onFailure(
            err -> {
              LOGGER.error("Failed to stream CSV", err);
              routingContext.fail(err);
            });
  }
}
