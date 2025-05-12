package org.cdpg.dx.auditing.activity.dao.impl;

import static org.cdpg.dx.auditing.activity.util.Constants.ACTIVITY_LOG_TABLE_NAME;
import static org.cdpg.dx.auditing.activity.util.Constants.USER_ID;

import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cdpg.dx.auditing.activity.dao.ActivityLogDao;
import org.cdpg.dx.auditing.activity.model.ActivityLog;
import org.cdpg.dx.auditing.activity.util.Constants;
import org.cdpg.dx.database.postgres.base.dao.AbstractBaseDAO;
import org.cdpg.dx.database.postgres.models.Condition;
import org.cdpg.dx.database.postgres.models.SelectQuery;
import org.cdpg.dx.database.postgres.service.PostgresService;

public class ActivityLogDaoImpl extends AbstractBaseDAO<ActivityLog> implements ActivityLogDao {
  private static final Logger LOGGER = LogManager.getLogger(ActivityLogDaoImpl.class);

  public ActivityLogDaoImpl(PostgresService postgresService) {
    super(postgresService, ACTIVITY_LOG_TABLE_NAME, Constants.ID, ActivityLog::fromJson);
  }

  @Override
  public Future<List<ActivityLog>> getAllActivityLogsByUserId(UUID userId) {

    Condition condition =
        new Condition(USER_ID, Condition.Operator.EQUALS, List.of(userId.toString()));

    SelectQuery query = new SelectQuery(ACTIVITY_LOG_TABLE_NAME, List.of("*"), condition);

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
  public Future<Void> createActivityLog(ActivityLog activityLog) {
    return create(activityLog).mapEmpty();
  }
}
