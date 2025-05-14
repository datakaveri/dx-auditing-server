package org.cdpg.dx.auditingserver.activity.controller;

import io.vertx.core.json.JsonArray;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.openapi.RouterBuilder;
import java.util.Optional;
import java.util.UUID;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cdpg.dx.auditingserver.activity.model.ActivityLog;
import org.cdpg.dx.auditingserver.activity.service.ActivityService;
import org.cdpg.dx.auditingserver.apiserver.ApiController;
import org.cdpg.dx.auth.authentication.model.JwtData;
import org.cdpg.dx.common.HttpStatusCode;
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
    builder.operation("getAllActivityLogs").handler(this::handleGetAllActivityLogs);
  }

  private void handleGetAllActivityLogs(RoutingContext context) {
    LOGGER.info("handleGetAllActivityLogs() started");

    Optional<JwtData> jwtDataOptional = RoutingContextHelper.getJwtData(context);
    if (jwtDataOptional.isEmpty()) {
      LOGGER.error("JWT data not found in context");
      ResponseBuilder.send(context, HttpStatusCode.UNAUTHORIZED, "JWT data missing", null);
      return;
    }
    JwtData jwtData = jwtDataOptional.get();
    UUID userId = UUID.fromString(jwtData.sub());

    activityService
        .getActivityLogByUserId(userId)
        .onSuccess(
            logs -> {
              if (logs.isEmpty()) {
                ResponseBuilder.sendNoContent(context);
              } else {
                LOGGER.info("Fetched activity logs successfully");
                ResponseBuilder.sendSuccess(context, mapActivityLogsToJsonArray(logs));
              }
            })
        .onFailure(
            failure -> {
              LOGGER.error("Failed to fetch activity logs: {}", failure.getMessage(), failure);
              context.fail(failure);
            });
  }

  private JsonArray mapActivityLogsToJsonArray(java.util.List<ActivityLog> logs) {
    return logs.stream()
        .map(ActivityLog::toJson)
        .collect(JsonArray::new, JsonArray::add, JsonArray::addAll);
  }
}
