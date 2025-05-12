package org.cdpg.dx.auditing.apiserver;

import io.vertx.core.Vertx;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.openapi.RouterBuilder;
import java.util.Optional;
import java.util.UUID;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cdpg.dx.auditing.activity.dao.impl.ActivityLogDaoImpl;
import org.cdpg.dx.auditing.activity.model.ActivityLog;
import org.cdpg.dx.auditing.activity.service.ActivityService;
import org.cdpg.dx.auditing.activity.service.ActivityServiceImpl;
import org.cdpg.dx.common.models.JwtData;
import org.cdpg.dx.database.postgres.service.PostgresService;
import org.cdpg.dx.util.RoutingContextHelper;

public class ActivityController implements ApiController {
  private static final Logger LOGGER = LogManager.getLogger(ActivityController.class);
  private final ActivityService activityService;
  private final Vertx vertx;

  public ActivityController(PostgresService pgService, Vertx vertx) {
    ActivityLogDaoImpl activityLogDAO = new ActivityLogDaoImpl(pgService);
    this.activityService = new ActivityServiceImpl(activityLogDAO);
    this.vertx = vertx;
  }

  @Override
  public void register(RouterBuilder builder) {
    builder
        .operation("getAllActivityLogs")
        .handler(this::handleGetAllActivityLogs)
        .failureHandler(this::handleFailure);
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
                          .map(ActivityLog::toJson)
                          .collect(JsonArray::new, JsonArray::add, JsonArray::addAll)
                          .encode());
            })
        .onFailure(
            failure -> {
              LOGGER.error("Failed to fetch activity logs: {}", failure.getMessage(), failure);
              context.fail(failure);
            });
  }

  private void handleFailure(RoutingContext context) {
    Throwable failure = context.failure();
    LOGGER.error("Request failed: {}", failure.getMessage(), failure);
    context
        .response()
        .setStatusCode(500)
        .end(new JsonObject().put("detail", failure.getMessage()).encode());
  }
}
