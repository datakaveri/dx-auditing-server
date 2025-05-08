package org.cdpg.dx.auditing.activity.dao.impl;

import static org.cdpg.dx.auditing.activity.util.Constants.ACTIVITY_LOG_TABLE_NAME;
import static org.cdpg.dx.auditing.activity.util.Constants.USER_ID;

import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cdpg.dx.auditing.activity.dao.ActivityLogDAO;
import org.cdpg.dx.auditing.activity.model.ActivityLog;
import org.cdpg.dx.auditing.activity.util.Constants;
import org.cdpg.dx.database.postgres.base.dao.AbstractBaseDAO;
import org.cdpg.dx.database.postgres.models.Condition;
import org.cdpg.dx.database.postgres.models.InsertQuery;
import org.cdpg.dx.database.postgres.models.SelectQuery;
import org.cdpg.dx.database.postgres.service.PostgresService;

public class ActivityLogDAOImpl extends AbstractBaseDAO<ActivityLog> implements ActivityLogDAO {
  private static final Logger LOGGER = LogManager.getLogger(ActivityLogDAOImpl.class);

  public ActivityLogDAOImpl(PostgresService postgresService) {
    super(postgresService, ACTIVITY_LOG_TABLE_NAME, Constants.ID, ActivityLog::fromJson);
  }

  @Override
  public Future<List<ActivityLog>> getAllActivityLogsByUserId(UUID userId) {

    Condition condition =
        new Condition(USER_ID, Condition.Operator.EQUALS, List.of(userId.toString()));

    SelectQuery query = new SelectQuery(tableName, List.of("*"), condition, null, null, null, null);

    return postgresService
        .select(query)
        .compose(
            result -> {
              List<ActivityLog> entities =
                  result.getRows().stream()
                      .map(row -> ActivityLog.fromJson((JsonObject) row))
                      .collect(Collectors.toList());
              return Future.succeededFuture(entities);
            })
        .recover(Future::failedFuture);
  }

  @Override
  public Future<ActivityLog> createActivityLog(ActivityLog activityLog) {
    Map<String, Object> dataMap = activityLog.toNonEmptyFieldsMap();
    InsertQuery query =
        new InsertQuery(tableName, List.copyOf(dataMap.keySet()), List.copyOf(dataMap.values()));

    return postgresService
        .insert(query)
        .compose(
            result -> {
              if (result.getRows().isEmpty()) {
                return Future.failedFuture("Insert query returned no rows.");
              }
              return Future.succeededFuture(fromJson.apply(result.getRows().getJsonObject(0)));
            })
        .recover(
            err -> {
              LOGGER.error("Error inserting to {}: msg: {}", tableName, err.getMessage(), err);
              return Future.failedFuture(err);
            });
  }
}
