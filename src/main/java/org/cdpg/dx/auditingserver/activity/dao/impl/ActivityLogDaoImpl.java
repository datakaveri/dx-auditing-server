package org.cdpg.dx.auditingserver.activity.dao.impl;

import static org.cdpg.dx.auditingserver.activity.util.ActivityConstants.*;

import io.vertx.core.Future;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cdpg.dx.auditingserver.activity.dao.ActivityLogDao;
import org.cdpg.dx.auditingserver.activity.model.ActivityLog;
import org.cdpg.dx.auditingserver.activity.model.Pagination;
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
  public Future<List<ActivityLog>> getAllActivityLogsByUserId(UUID userId) {
    Condition condition =
        new Condition(USER_ID, Condition.Operator.EQUALS, List.of(userId.toString()));

    SelectQuery query =
        new SelectQuery(ACTIVITY_LOG_TABLE_NAME, List.of("*"), condition, null, null, null, null);

    return postgresService
        .select(query, false)
        .compose(
            result -> {
              List<ActivityLog> entities = mapToActivityLogs(result.getRows());
              LOGGER.debug("Fetched {} rows for userId {}", entities.size(), userId);

              return Future.succeededFuture(entities);
            })
        .recover(
            err -> {
              LOGGER.error(
                  "Error fetching activity logs for user ID {} from {}: {}",
                  userId,
                  tableName,
                  err.getMessage(),
                  err);
              return Future.failedFuture(DxPgExceptionMapper.from(err));
            });
  }

  @Override
  public Future<ActivityLog> createActivityLog(ActivityLog activityLogEntity) {
    return create(activityLogEntity);
  }

  @Override
  public Future<List<ActivityLog>> getAllActivityLogsForAdmin() {
    LOGGER.info("getAllActivityLogsForAdmin() called");

    SelectQuery query =
        new SelectQuery(ACTIVITY_LOG_TABLE_NAME, MINIMAL_COLUMNS, null, null, null, null, null);

    return postgresService
        .select(query, false)
        .compose(
            result -> {
              List<ActivityLog> entities = mapToActivityLogs(result.getRows());
              LOGGER.debug("Fetched {} activity logs for admin", entities.size());
              return Future.succeededFuture(entities);
            })
        .recover(
            err -> {
              LOGGER.error(
                  "Error fetching activity logs for admin from {}: {}",
                  tableName,
                  err.getMessage(),
                  err);
              return Future.failedFuture(DxPgExceptionMapper.from(err));
            });
  }

  public Future<Pagination<ActivityLog>> getAllWitPagination(int limit, int offset) {
    int size = limit > 0 ? limit : 10;
    int page = (limit > 0) ? (offset / limit) + 1 : 1;

    SelectQuery query =
        new SelectQuery(ACTIVITY_LOG_TABLE_NAME, MINIMAL_COLUMNS, null, null, null, size, offset);

    return postgresService
        .select(query, true)
        .map(
            result -> {
              List<ActivityLog> data = mapToActivityLogs(result.getRows());

              long totalCount = result.getTotalCount();
              int totalPages = (int) Math.ceil((double) totalCount / size);
              boolean hasNext = page < totalPages;
              boolean hasPrevious = page > 1;

              return new Pagination<>(
                  page, size, totalCount, totalPages, hasNext, hasPrevious, data);
            })
        .recover(
            err -> {
              LOGGER.error("Error fetching paginated activity logs: {}", err.getMessage(), err);
              return Future.failedFuture(DxPgExceptionMapper.from(err));
            });
  }

  /**
   * Maps a JsonArray of rows to a List of ActivityLog objects.
   *
   * @param rows The JsonArray containing the rows to be mapped.
   * @return A List of ActivityLog objects.
   */
  private List<ActivityLog> mapToActivityLogs(JsonArray rows) {
    return rows.stream()
        .map(obj -> ActivityLog.fromJson((JsonObject) obj))
        .collect(Collectors.toList());
  }
}
