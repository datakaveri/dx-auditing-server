package org.cdpg.dx.auditingserver.activity.controller;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.auth.User;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.openapi.RouterBuilder;
import java.util.List;
import java.util.UUID;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cdpg.dx.auditingserver.activity.model.ActivityLog;
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

    activityService
        .getActivityLogByUserId(userId)
        .onSuccess(
            logs -> {
              if (logs.isEmpty()) {
                ResponseBuilder.sendNoContent(context);
              } else {
                LOGGER.info("Fetched activity logs successfully for consumer user: {}", userId);
                ResponseBuilder.sendSuccess(context, logs);
              }
            })
        .onFailure(
            failure -> {
              LOGGER.error("Failed to fetch activity logs: {}", failure.getMessage(), failure);
              context.fail(failure);
            });
  }

  private void handleGetAllActivityLogsForAdmin(RoutingContext context) {
    LOGGER.info("handleGetAllActivityLogsForAdmin() started");
    int offset = Integer.parseInt(context.request().getParam("offset"));
    int limit = Integer.parseInt(context.request().getParam("limit"));
    activityService
        .getAllWitPagination(limit, offset)
            .onSuccess(pagination -> {
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

  private JsonArray mapActivityLogsToJsonArray(List<ActivityLog> logs) {
    return logs.stream()
        .map(ActivityLog::toJson)
        .collect(JsonArray::new, JsonArray::add, JsonArray::addAll);
  }
}
