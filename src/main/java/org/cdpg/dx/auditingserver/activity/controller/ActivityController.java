package org.cdpg.dx.auditingserver.activity.controller;

import static org.cdpg.dx.auditingserver.activity.util.ActivityConstants.*;
import static org.cdpg.dx.database.postgres.util.Constants.DEFAULT_SORTIMG_ORDER;

import io.vertx.core.Handler;
import io.vertx.ext.auth.User;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.openapi.RouterBuilder;
import java.util.Map;
import java.util.Set;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cdpg.dx.auditingserver.activity.service.ActivityService;
import org.cdpg.dx.auditingserver.apiserver.ApiController;
import org.cdpg.dx.auth.authorization.handler.AuthorizationHandler;
import org.cdpg.dx.auth.authorization.model.DxRole;
import org.cdpg.dx.common.request.PaginatedRequest;
import org.cdpg.dx.common.request.PaginationRequestBuilder;
import org.cdpg.dx.common.request.PaginationRequestConfig;
import org.cdpg.dx.common.response.ResponseBuilder;
import org.cdpg.dx.common.validator.QueryParamValidationHandler;

public class ActivityController implements ApiController {
  private static final Logger LOGGER = LogManager.getLogger(ActivityController.class);
  private final ActivityService activityService;

  public ActivityController(ActivityService activityService) {
    this.activityService = activityService;
  }

  @Override
  public void register(RouterBuilder builder) {

    QueryParamValidationHandler ConsumerParamValidationHandler =
        new QueryParamValidationHandler(allowedQueryParamsForConsumer);
    QueryParamValidationHandler adminParamValidationHandler =
        new QueryParamValidationHandler(allowedQueryParamsForAdmin);

    Handler<RoutingContext> adminAccessHandler =
        AuthorizationHandler.forRoles(DxRole.ORG_ADMIN, DxRole.COS_ADMIN);
    Handler<RoutingContext> consumerAccessHandler = AuthorizationHandler.forRoles(DxRole.CONSUMER);

    builder
        .operation("get-ActivityLogs-for-consumer")
        .handler(ConsumerParamValidationHandler)
        .handler(consumerAccessHandler)
        .handler(this::handleGetAllActivityLogsForUser);
    builder
        .operation("get-activityLogs-for-admin")
        .handler(adminParamValidationHandler)
        .handler(adminAccessHandler)
        .handler(this::handleGetAllActivityLogsForAdmin);
  }

  private void handleGetAllActivityLogsForUser(RoutingContext context) {
    LOGGER.info("handleGetAllActivityLogsForUser() started");

    User user = context.user();

    Set<String> allowedFilters = Set.of("userId", "assetType", "operation");

    Map<String, Object> additionalFilters =
        Map.of("user_id", user.subject(), "myactivity_enabled", true);

    Set<String> allowedTimeFields = Set.of("created_at");
    Set<String> allowedSortFields = Set.of("createdAt", "UserId", "assetType", "operation");

    PaginationRequestConfig config =
        new PaginationRequestConfig.Builder()
            .ctx(context)
            .allowedFilterKeys(allowedFilters)
            .apiToDbMap(API_TO_DB_MAP)
            .additionalFilters(additionalFilters)
            .allowedTimeFields(allowedTimeFields)
            .defaultTimeField("created_at")
            .defaultSortBy("created_at")
            .defaultOrder(DEFAULT_SORTIMG_ORDER)
            .allowedSortFields(allowedSortFields)
            .build();

    PaginatedRequest request = PaginationRequestBuilder.fromRoutingContext(config);

    LOGGER.info("PaginatedRequest created for getActivityLogForConsumer:  {}", request);

    activityService
        .getActivityLogForConsumer(request)
        .onSuccess(
            pagedResult -> {
              LOGGER.info("Successfully fetched activity logs for user: {}", user.subject());
              ResponseBuilder.sendSuccess(
                  context, pagedResult.data(), pagedResult.paginationInfo());
            })
        .onFailure(
            failure -> {
              LOGGER.error("Failed to fetch activity logs: {}", failure.getMessage(), failure);
              context.fail(failure);
            });
  }

  private void handleGetAllActivityLogsForAdmin(RoutingContext context) {
    LOGGER.info("handleGetAllActivityLogsForAdmin() started");

    Set<String> allowedFilters = Set.of("userId", "assetType", "operation");
    Set<String> allowedTimeFields = Set.of("created_at");
    Set<String> allowedSortFields = Set.of("createdAt", "UserId", "assetType", "operation");
    Map<String, Object> additionalFilters = Map.of("myactivity_enabled", true);

    PaginationRequestConfig config =
        new PaginationRequestConfig.Builder()
            .ctx(context)
            .allowedFilterKeys(allowedFilters)
            .apiToDbMap(API_TO_DB_MAP)
            .allowedTimeFields(allowedTimeFields)
            .defaultTimeField("created_at")
            .defaultSortBy("created_at")
            .defaultOrder(DEFAULT_SORTIMG_ORDER)
            .allowedSortFields(allowedSortFields)
            .additionalFilters(additionalFilters)
            .build();
    PaginatedRequest request = PaginationRequestBuilder.fromRoutingContext(config);

    LOGGER.info("PaginatedRequest created for handleGetAllActivityLogsForAdmin:  {}", request);

    activityService
        .getAllActivityLogsForAdmin(request)
        .onSuccess(
            pagedResult -> {
              LOGGER.info("Successfully fetched all activity logs for admin");
              ResponseBuilder.sendSuccess(
                  context, pagedResult.data(), pagedResult.paginationInfo());
            })
        .onFailure(
            failure -> {
              LOGGER.error("Failed to fetch activity logs: {}", failure.getMessage(), failure);
              context.fail(failure);
            });
  }
}
