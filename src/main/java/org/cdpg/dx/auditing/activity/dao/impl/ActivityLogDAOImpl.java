package org.cdpg.dx.auditing.activity.dao.impl;

import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import org.cdpg.dx.auditing.activity.dao.ActivityLogDAO;
import org.cdpg.dx.auditing.activity.model.ActivityLog;
import org.cdpg.dx.auditing.activity.util.Constants;
import org.cdpg.dx.database.postgres.base.dao.AbstractBaseDAO;
import org.cdpg.dx.database.postgres.models.SelectQuery;
import org.cdpg.dx.database.postgres.service.PostgresService;

public class ActivityLogDAOImpl extends AbstractBaseDAO<ActivityLog> implements ActivityLogDAO {

  public ActivityLogDAOImpl(PostgresService postgresService) {
    super(postgresService, ActivityLog.TABLE_NAME, Constants.ID, ActivityLog::fromJson);
  }

  @Override
  public Future<List<ActivityLog>> getAllActivityLogsByUserId(UUID userId) {
    SelectQuery query = new SelectQuery(tableName, List.of("*"), null, null, null, null, null);

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
        .recover(
                Future::failedFuture);
  }
}
