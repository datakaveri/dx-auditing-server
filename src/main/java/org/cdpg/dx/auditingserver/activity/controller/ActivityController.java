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
import org.cdpg.dx.auditingserver.common.ControllerUtil;
import org.cdpg.dx.auth.authorization.handler.AuthorizationHandler;
import org.cdpg.dx.auth.authorization.model.DxRole;
import org.cdpg.dx.common.model.DxUser;
import org.cdpg.dx.common.request.PaginatedRequest;
import org.cdpg.dx.common.request.PaginationRequestBuilder;
import org.cdpg.dx.common.response.ResponseBuilder;
import org.cdpg.dx.util.RoutingContextHelper;

public class ActivityController implements ApiController {
  private static final Logger LOGGER = LogManager.getLogger(ActivityController.class);
  private final ActivityService activityService;

  public ActivityController(ActivityService activityService) {
    this.activityService = activityService;
  }

  @Override
  public void register(RouterBuilder builder) {

    Handler<RoutingContext> adminAccessHandler =
        AuthorizationHandler.forRoles(DxRole.ORG_ADMIN, DxRole.COS_ADMIN);
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

    Map<String, Object> additionalFilters =
        Map.of(USER_ID, user.subject(), MYACTIVITY_ENABLED, true);

    PaginatedRequest request =
        PaginationRequestBuilder.from(context)
            .allowedFiltersDbMap(ALLOWED_FILTER_MAP_FOR_USER)
            .additionalFilters(additionalFilters)
            .allowedTimeFields(Set.of(CREATED_AT))
            .defaultTimeField(CREATED_AT)
            .defaultSort(CREATED_AT, DEFAULT_SORTIMG_ORDER)
            .allowedSortFields(ALLOWED_SORT_FEILDS)
            .build();

    LOGGER.info("PaginatedRequest created for getActivityLogForUser:  {}", request);

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

    DxUser user = RoutingContextHelper.fromPrincipal(context);

    Map<String, Object> additionalFilters = ControllerUtil.getAdditionalFilters(user);
    Map<String, String> allowedFilterMap = ControllerUtil.getAllowedFilterMapForAdmin(user);

    PaginatedRequest request =
        PaginationRequestBuilder.from(context)
            .allowedFiltersDbMap(allowedFilterMap)
            .additionalFilters(additionalFilters)
            .allowedTimeFields(Set.of(CREATED_AT))
            .defaultTimeField(CREATED_AT)
            .defaultSort(CREATED_AT, DEFAULT_SORTIMG_ORDER)
            .allowedSortFields(ALLOWED_SORT_FEILDS)
            .build();

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
