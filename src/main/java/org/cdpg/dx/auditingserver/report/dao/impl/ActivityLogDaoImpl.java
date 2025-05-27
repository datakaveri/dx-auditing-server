package org.cdpg.dx.auditingserver.report.dao.impl;

import static org.cdpg.dx.auditingserver.activity.util.ActivityConstants.*;

import io.vertx.core.Future;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cdpg.dx.auditingserver.report.dao.ActivityLogDao;
import org.cdpg.dx.auditingserver.report.model.ActivityLog;
import org.cdpg.dx.database.postgres.base.dao.AbstractBaseDAO;
import org.cdpg.dx.database.postgres.models.Condition;
import org.cdpg.dx.database.postgres.models.SelectQuery;
import org.cdpg.dx.database.postgres.service.PostgresService;
import org.cdpg.dx.database.postgres.util.DxPgExceptionMapper;

public class ActivityLogDaoImpl extends AbstractBaseDAO<ActivityLog> implements ActivityLogDao {
  private static final Logger LOGGER = LogManager.getLogger(ActivityLogDaoImpl.class);
  private static final List<String> MINIMAL_COLUMNS =
      List.of("id", "user_id", "asset_name", "operation");

  public ActivityLogDaoImpl(PostgresService postgresService) {
    super(postgresService, ACTIVITY_LOG_TABLE_NAME, ID, ActivityLog::fromJson);
  }

  @Override
  public Future<List<ActivityLog>> getCsvGeneratedByUserId(UUID userId) {
    Condition condition =
        new Condition(USER_ID, Condition.Operator.EQUALS, List.of(userId.toString()));

    SelectQuery query =
        new SelectQuery(ACTIVITY_LOG_TABLE_NAME, List.of("*"), condition, null, null, null, null);

    return postgresService
        .select(query, true)
        .compose(
            result -> {
              LOGGER.trace("total counts :: " + result.getTotalCount());
              List<ActivityLog> entities = mapToActivityLogs(result.getRows());
              LOGGER.debug("Fetched {} activity logs for csv", entities.size());
              return Future.succeededFuture(entities);
            })
        .recover(
            err -> {
              LOGGER.error(
                  "Error fetching activity logs for csv from {}: {}",
                  tableName,
                  err.getMessage(),
                  err);
              return Future.failedFuture(DxPgExceptionMapper.from(err));
            });
  }

  @Override
  public Future<List<ActivityLog>> getCsvGenerateForAdmin() {
    SelectQuery query =
        new SelectQuery(ACTIVITY_LOG_TABLE_NAME, List.of("*"), null, null, null, null, null);

    return postgresService
        .select(query, true)
        .compose(
            result -> {
              LOGGER.trace("total counts :: " + result.getTotalCount());
              List<ActivityLog> entities = mapToActivityLogs(result.getRows());
              LOGGER.debug("Fetched {} activity logs for csv", entities.size());
              return Future.succeededFuture(entities);
            })
        .recover(
            err -> {
              LOGGER.error(
                  "Error fetching activity logs for csv from {}: {}",
                  tableName,
                  err.getMessage(),
                  err);
              return Future.failedFuture(DxPgExceptionMapper.from(err));
            });
  }

  private List<ActivityLog> mapToActivityLogs(JsonArray rows) {
    return rows.stream()
        .map(obj -> ActivityLog.fromJson((JsonObject) obj))
        .collect(Collectors.toList());
  }
}
