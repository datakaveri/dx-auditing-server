package org.cdpg.dx.auditingserver.apiserver;

import io.vertx.core.json.JsonArray;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.openapi.RouterBuilder;
import java.util.Optional;
import java.util.UUID;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cdpg.dx.auditingserver.activity.model.ActivityLogEntity;
import org.cdpg.dx.auditingserver.activity.service.ActivityService;
import org.cdpg.dx.auth.authentication.model.JwtData;
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
    LOGGER.info("Handling getAllActivityLogs request...");

    Optional<JwtData> jwtDataOptional = RoutingContextHelper.getJwtData(context);
    JwtData jwtData = jwtDataOptional.get();
    UUID userId = UUID.fromString(jwtData.sub());

    activityService
        .getActivityLogByUserId(userId)
        .onSuccess(
            logs -> {
              LOGGER.info("Fetched activity logs successfully");


              LOGGER.info("logs: {}", logs.get(0).toJson());

              context
                  .response()
                  .setStatusCode(200)
                  .putHeader("Content-Type", "application/json")
                  .end(
                      logs.stream()
                          .map(ActivityLogEntity::toJson)
                          .collect(JsonArray::new, JsonArray::add, JsonArray::addAll)
                          .encode());
            })
        .onFailure(
            failure -> {
              LOGGER.error("Failed to fetch activity logs: {}", failure.getMessage(), failure);
              context.fail(failure);
            });
  }
}
