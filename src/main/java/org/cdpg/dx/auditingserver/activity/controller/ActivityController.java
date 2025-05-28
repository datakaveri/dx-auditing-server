package org.cdpg.dx.auditingserver.activity.controller;

import io.vertx.ext.auth.User;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.openapi.RouterBuilder;
import java.util.UUID;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cdpg.dx.auditingserver.activity.model.ActivityLog;
import org.cdpg.dx.auditingserver.activity.model.ActivityLogAdminRequest;
import org.cdpg.dx.auditingserver.activity.model.ActivityLogAdminResponse;
import org.cdpg.dx.auditingserver.activity.service.ActivityService;
import org.cdpg.dx.auditingserver.apiserver.ApiController;
import org.cdpg.dx.common.response.ResponseBuilder;

public class ActivityController implements ApiController {
  private static final Logger LOGGER = LogManager.getLogger(ActivityController.class);
  private final ActivityService activityService;

  public ActivityController(ActivityService activityService) {
    this.activityService = activityService;
  }

  @Override
  public void register(RouterBuilder builder) {
    builder
        .operation("get-ActivityLogs-for-consumer-user")
        .handler(this::handleGetAllActivityLogsForUser);
    builder
        .operation("get-activityLogs-for-admin-user")
        .handler(this::handleGetAllActivityLogsForAdmin);
  }

  private void handleGetAllActivityLogsForUser(RoutingContext context) {
    LOGGER.info("handleGetAllActivityLogsForUser() started");

    User user = context.user();
    UUID userId = UUID.fromString(user.subject());

    String limitParam = context.request().getParam("limit");
    int limit = (limitParam != null && !limitParam.isEmpty()) ? Integer.parseInt(limitParam) : 1000;

    String offsetParam = context.request().getParam("offset");
    int offset =
        (offsetParam != null && !offsetParam.isEmpty()) ? Integer.parseInt(offsetParam) : 0;

    activityService
        .getActivityLogByUserId(userId, limit, offset)
        .onSuccess(
            pagination -> {
              ActivityLogAdminResponse<ActivityLog> response =
                  new ActivityLogAdminResponse<>(pagination);
              ResponseBuilder.sendSuccess(context, response);
            })
        .onFailure(
            failure -> {
              LOGGER.error("Failed to fetch activity logs: {}", failure.getMessage(), failure);
              context.fail(failure);
            });
  }

  private void handleGetAllActivityLogsForAdmin(RoutingContext context) {
    LOGGER.info("handleGetAllActivityLogsForAdmin() started");
    UUID userId = null;
    String userIdParam = context.request().getParam("userId");
    if (userIdParam != null && !userIdParam.isEmpty()) {
      userId = UUID.fromString(userIdParam);
    }

    String startTime = context.request().getParam("starttime");
    String endTime = context.request().getParam("endtime");

    String limitParam = context.request().getParam("limit");
    int limit = (limitParam != null && !limitParam.isEmpty()) ? Integer.parseInt(limitParam) : 1000;

    String offsetParam = context.request().getParam("offset");
    int offset =
        (offsetParam != null && !offsetParam.isEmpty()) ? Integer.parseInt(offsetParam) : 0;
    ActivityLogAdminRequest activityLogAdminRequest =
        new ActivityLogAdminRequest(userId, startTime, endTime, limit, offset);

    activityService
        .getAllActivityLogsForAdmin(activityLogAdminRequest)
        .onSuccess(
            pagination -> {
              ActivityLogAdminResponse<ActivityLog> response =
                  new ActivityLogAdminResponse<>(pagination);
              ResponseBuilder.sendSuccess(context, response);
            })
        .onFailure(
            failure -> {
              LOGGER.error("Failed to fetch activity logs: {}", failure.getMessage(), failure);
              context.fail(failure);
            });
  }
}
