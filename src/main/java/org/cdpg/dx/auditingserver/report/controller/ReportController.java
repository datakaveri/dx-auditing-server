package org.cdpg.dx.auditingserver.report.controller;

import static org.cdpg.dx.auditingserver.activity.util.ActivityConstants.*;
import static org.cdpg.dx.database.postgres.util.Constants.DEFAULT_SORTIMG_ORDER;

import io.vertx.core.Handler;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.ext.auth.User;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.openapi.RouterBuilder;
import java.util.Map;
import java.util.Set;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cdpg.dx.auditingserver.apiserver.ApiController;
import org.cdpg.dx.auditingserver.common.ControllerUtil;
import org.cdpg.dx.auditingserver.report.service.ReportService;
import org.cdpg.dx.auth.authorization.handler.AuthorizationHandler;
import org.cdpg.dx.auth.authorization.model.DxRole;
import org.cdpg.dx.common.model.DxUser;
import org.cdpg.dx.common.request.PaginatedRequest;
import org.cdpg.dx.common.request.PaginationRequestBuilder;
import org.cdpg.dx.util.RoutingContextHelper;

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

    DxUser user = RoutingContextHelper.fromPrincipal(routingContext);

    Map<String, Object> additionalFilters = ControllerUtil.getAdditionalFilters(user);
    Map<String, String> allowedFilterMap = ControllerUtil.getAllowedFilterMapForAdmin(user);

    PaginatedRequest request =
        PaginationRequestBuilder.from(routingContext)
            .allowedFiltersDbMap(allowedFilterMap)
            .additionalFilters(additionalFilters)
            .allowedTimeFields(Set.of(CREATED_AT))
            .defaultTimeField(CREATED_AT)
            .defaultSort(CREATED_AT, DEFAULT_SORTIMG_ORDER)
            .allowedSortFields(ALLOWED_SORT_FEILDS)
            .build();

    LOGGER.info("PaginatedRequest created for handleGetAllActivityLogsForAdmin:  {}", request);
    reportService
        .streamAdminCsvBatched(request)
        .onSuccess(
            csvStream -> {
              if (csvStream == null) {
                response.end();
                return;
              }
              csvStream
                  .exceptionHandler(
                      err -> {
                        LOGGER.error("Failed to stream CSV", err);
                        routingContext.fail(err);
                      })
                  .handler(buffer -> response.write(buffer))
                  .endHandler(v -> response.end());
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
    Map<String, Object> additionalFilters =
        Map.of("user_id", user.subject(), "myactivity_enabled", true);

    PaginatedRequest request =
        PaginationRequestBuilder.from(routingContext)
            .allowedFiltersDbMap(ALLOWED_FILTER_MAP_FOR_USER)
            .additionalFilters(additionalFilters)
            .allowedTimeFields(Set.of(CREATED_AT))
            .defaultTimeField(CREATED_AT)
            .defaultSort(CREATED_AT, DEFAULT_SORTIMG_ORDER)
            .allowedSortFields(ALLOWED_SORT_FEILDS)
            .build();

    reportService
        .streamConsumerCsvBatched(request)
        .onSuccess(
            csvStream -> {
              if (csvStream == null) {
                response.end();
                return;
              }
              csvStream
                  .exceptionHandler(
                      err -> {
                        LOGGER.error("Failed to stream CSV", err);
                        routingContext.fail(err);
                      })
                  .handler(buffer -> response.write(buffer))
                  .endHandler(v -> response.end());
            })
        .onFailure(
            err -> {
              LOGGER.error("Failed to stream CSV", err);
              routingContext.fail(err);
            });
  }
}
