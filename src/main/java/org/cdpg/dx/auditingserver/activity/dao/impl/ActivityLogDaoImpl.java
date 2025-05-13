package org.cdpg.dx.auditingserver.activity.dao.impl;

import static org.cdpg.dx.auditingserver.activity.util.ActivityConstants.ACTIVITY_LOG_TABLE_NAME;
import static org.cdpg.dx.auditingserver.activity.util.ActivityConstants.USER_ID;

import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cdpg.dx.auditingserver.activity.dao.ActivityLogDao;
import org.cdpg.dx.auditingserver.activity.model.ActivityLogEntity;
import org.cdpg.dx.auditingserver.activity.util.ActivityConstants;
import org.cdpg.dx.database.postgres.base.dao.AbstractBaseDAO;
import org.cdpg.dx.database.postgres.models.Condition;
import org.cdpg.dx.database.postgres.models.SelectQuery;
import org.cdpg.dx.database.postgres.service.PostgresService;

public class ActivityLogDaoImpl extends AbstractBaseDAO<ActivityLogEntity> implements ActivityLogDao {
  private static final Logger LOGGER = LogManager.getLogger(ActivityLogDaoImpl.class);

  public ActivityLogDaoImpl(PostgresService postgresService) {
    super(postgresService, ACTIVITY_LOG_TABLE_NAME, ActivityConstants.ID, ActivityLogEntity::fromJson);
  }

  @Override
  public Future<List<ActivityLogEntity>> getAllActivityLogsByUserId(UUID userId) {

    Condition condition =
        new Condition(USER_ID, Condition.Operator.EQUALS, List.of(userId.toString()));

    SelectQuery query =
        new SelectQuery(ACTIVITY_LOG_TABLE_NAME, List.of("##"), condition, null, null, null, null);

    return postgresService
        .select(query)
        .compose(
            result -> {
              List<ActivityLogEntity> entities =
                  result.getRows().stream()
                      .map(row -> ActivityLogEntity.fromJson((JsonObject) row))
                      .collect(Collectors.toList());
              return Future.succeededFuture(entities);
            })
        .recover(Future::failedFuture);
  }

  @Override
  public Future<ActivityLogEntity> createActivityLog(ActivityLogEntity activityLogEntity) {
    return create(activityLogEntity);
  }
}
