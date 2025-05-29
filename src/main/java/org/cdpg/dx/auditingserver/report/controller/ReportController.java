// src/main/java/org/cdpg/dx/auditingserver/report/controller/ReportController.java
package org.cdpg.dx.auditingserver.report.controller;

import io.vertx.core.http.HttpServerResponse;
import io.vertx.ext.auth.User;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.openapi.RouterBuilder;
import java.util.UUID;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cdpg.dx.auditingserver.apiserver.ApiController;
import org.cdpg.dx.auditingserver.report.service.ReportService;

public class ReportController implements ApiController {
  private static final Logger LOGGER = LogManager.getLogger(ReportController.class);
  private final ReportService reportService;

  public ReportController(ReportService reportService) {
    this.reportService = reportService;
  }

  @Override
  public void register(RouterBuilder builder) {
    builder.operation("get-admin-report").handler(this::handleGenerateCsvForAdmin);
    builder.operation("get-consumer-report").handler(this::handleGenerateCsvForConsumer);
  }

  private void handleGenerateCsvForConsumer(RoutingContext routingContext) {
    HttpServerResponse response = routingContext.response();
    response
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

  private void handleGenerateCsvForAdmin(RoutingContext ctx) {
    HttpServerResponse response = ctx.response();
    response
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
                    ctx.fail(err);
                  });
            })
        .onFailure(
            err -> {
              LOGGER.error("Failed to stream CSV", err);
              ctx.fail(err);
            });
  }
}
