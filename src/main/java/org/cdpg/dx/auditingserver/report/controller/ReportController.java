package org.cdpg.dx.auditingserver.report.controller;

import static org.cdpg.dx.auditingserver.report.util.Constants.EMPTY_FILE;
import static org.cdpg.dx.auditingserver.report.util.Constants.TOO_MANY_ROWS;

import io.vertx.core.file.OpenOptions;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.ext.auth.User;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.openapi.RouterBuilder;
import java.util.UUID;

import org.apache.http.HttpStatus;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cdpg.dx.auditingserver.apiserver.ApiController;
import org.cdpg.dx.auditingserver.report.service.ReportService;
import org.cdpg.dx.common.HttpStatusCode;
import org.cdpg.dx.common.response.ResponseBuilder;

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

  private void handleGenerateCsvForAdmin(RoutingContext routingContext) {
    LOGGER.trace("handleGenerateCsvForAdminChunk started");
    HttpServerResponse response = routingContext.response();
    reportService
        .getAdminCsvReport()
        .onSuccess(
            filePath -> {
              if (filePath.equalsIgnoreCase(TOO_MANY_ROWS)) {
                ResponseBuilder.send(routingContext, HttpStatusCode.BAD_REQUEST, TOO_MANY_ROWS , null);
                return;
              } else if (filePath.equalsIgnoreCase(EMPTY_FILE)) {
                ResponseBuilder.sendNoContent(routingContext);
                return;
              }

              LOGGER.info("CSV file generated successfully at: {}", filePath);
              String fileName = filePath.substring(filePath.lastIndexOf('/') + 1);
              response
                  .putHeader("Content-Type", "text/csv")
                  .putHeader("Content-Disposition", "attachment; filename=\"" + fileName + "\"")
                  .setChunked(true);

              routingContext
                  .vertx()
                  .fileSystem()
                  .open(filePath, new OpenOptions())
                  .onComplete(
                      ar -> {
                        if (ar.succeeded()) {
                          ar.result()
                              .pipeTo(response)
                              .onFailure(
                                  err -> {
                                    LOGGER.error("Failed to stream file", err);
                                    routingContext.fail(err);
                                  });
                        } else {
                          LOGGER.error("Failed to open file", ar.cause());
                          routingContext.fail(ar.cause());
                        }
                      });
            })
        .onFailure(
            failure -> {
              LOGGER.error("Failed to generate CSV file", failure);
              routingContext.fail(failure);
            });
  }

  private void handleGenerateCsvForConsumer(RoutingContext routingContext) {
    HttpServerResponse response = routingContext.response();
    User user = routingContext.user();
    UUID userId = UUID.fromString(user.subject());
    reportService
        .getConsumerCsvReport(userId)
        .onSuccess(
            filePath -> {
              if (filePath.equalsIgnoreCase(TOO_MANY_ROWS)) {
                  ResponseBuilder.send(routingContext, HttpStatusCode.BAD_REQUEST, TOO_MANY_ROWS , null);
                return;
              } else if (filePath.equalsIgnoreCase(EMPTY_FILE)) {
                  ResponseBuilder.sendSuccess(routingContext,EMPTY_FILE);
                return;
              }
              LOGGER.info("CSV file generated successfully at: {}", filePath);
              String fileName = filePath.substring(filePath.lastIndexOf('/') + 1);
              response
                  .putHeader("Content-Type", "text/csv")
                  .putHeader("Content-Disposition", "attachment; filename=\"" + fileName + "\"")
                  .setChunked(true);

              routingContext
                  .vertx()
                  .fileSystem()
                  .open(filePath, new OpenOptions())
                  .onComplete(
                      ar -> {
                        if (ar.succeeded()) {
                          ar.result()
                              .pipeTo(response)
                              .onFailure(
                                  err -> {
                                    LOGGER.error("Failed to stream file", err);
                                    routingContext.fail(err);
                                  });
                        } else {
                          LOGGER.error("Failed to open file", ar.cause());
                          routingContext.fail(ar.cause());
                        }
                      });
            })
        .onFailure(
            failure -> {
              LOGGER.error("Failed to generate CSV file");
              routingContext.fail(failure);
            });
  }
}
