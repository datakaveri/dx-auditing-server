package org.cdpg.dx.auditingserver.activity.controller;

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
import org.cdpg.dx.common.response.PaginationInfo;
import org.cdpg.dx.common.response.ResponseBuilder;

public class ActivityController implements ApiController {
  private static final Logger LOGGER = LogManager.getLogger(ActivityController.class);
  private final ActivityService activityService;

  public ActivityController(ActivityService activityService) {
    this.activityService = activityService;
  }

  @Override
  public void register(RouterBuilder builder) {

    Handler<RoutingContext> adminAccessHandler =
        AuthorizationHandler.forRoles(DxRole.ORG_ADMIN, DxRole.ORG_ADMIN);
    Handler<RoutingContext> consumerAccessHandler = AuthorizationHandler.forRoles(DxRole.CONSUMER);

    builder
        .operation("get-ActivityLogs-for-consumer")
        .handler(consumerAccessHandler)
        .handler(this::handleGetAllActivityLogsForUser);
    builder
        .operation("get-activityLogs-for-admin")
        .handler(adminAccessHandler)
        .handler(this::handleGetAllActivityLogsForAdmin);
  }

  private void handleGetAllActivityLogsForUser(RoutingContext context) {
    LOGGER.info("handleGetAllActivityLogsForUser() started");

    User user = context.user();

    Set<String> allowedFilters = Set.of("userId");
    Map<String, String> apiToDbMap = Map.of("userId", "user_id");

    Map<String, String> additionalFilters = Map.of("user_id", user.subject());

    Set<String> allowedTimeFields = Set.of("created_at");

    PaginatedRequest request =
        PaginationRequestBuilder.fromRoutingContext(
            context,
            allowedFilters,
            apiToDbMap,
            additionalFilters,
            allowedTimeFields,
            "created_at");

    LOGGER.info("PaginatedRequest created for getActivityLogForConsumer:  {}", request);

    activityService
        .getActivityLogForConsumer(request)
        .onSuccess(
            pagedResult -> {
              LOGGER.info("Successfully fetched activity logs for user: {}", user.subject());
              ResponseBuilder.sendSuccess(
                  context, pagedResult.data(), PaginationInfo.fromPagedResult(pagedResult));
            })
        .onFailure(
            failure -> {
              LOGGER.error("Failed to fetch activity logs: {}", failure.getMessage(), failure);
              context.fail(failure);
            });
  }

  private void handleGetAllActivityLogsForAdmin(RoutingContext context) {
    LOGGER.info("handleGetAllActivityLogsForAdmin() started");
    /*    Optional<ActivityLogRequest> optReq = validateAndExtractAdminActivityParams(context, null);
    if (optReq.isEmpty()) return;*/

    Set<String> allowedFilters = Set.of("userId");
    Map<String, String> apiToDbMap = Map.of("userId", "user_id");
    // No additional filters for admin, as they can access all logs
    Set<String> allowedTimeFields = Set.of("created_at");

    PaginatedRequest request =
        PaginationRequestBuilder.fromRoutingContext(
            context, allowedFilters, apiToDbMap, null, allowedTimeFields, "created_at");

    LOGGER.info("PaginatedRequest created for handleGetAllActivityLogsForAdmin:  {}", request);

    activityService
        .getAllActivityLogsForAdmin(request)
        .onSuccess(
            pagedResult -> {
              LOGGER.info("Successfully fetched all activity logs for admin");
              ResponseBuilder.sendSuccess(
                  context, pagedResult.data(), PaginationInfo.fromPagedResult(pagedResult));
            })
        .onFailure(
            failure -> {
              LOGGER.error("Failed to fetch activity logs: {}", failure.getMessage(), failure);
              context.fail(failure);
            });
  }
}
