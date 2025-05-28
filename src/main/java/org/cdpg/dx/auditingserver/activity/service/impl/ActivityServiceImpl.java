package org.cdpg.dx.auditingserver.activity.service.impl;

import io.vertx.core.Future;
import java.util.List;
import java.util.UUID;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cdpg.dx.auditingserver.activity.model.Pagination;
import org.cdpg.dx.auditingserver.activity.dao.ActivityLogDao;
import org.cdpg.dx.auditingserver.activity.model.ActivityLog;
import org.cdpg.dx.auditingserver.activity.service.ActivityService;

public class ActivityServiceImpl implements ActivityService {
  private static final Logger LOGGER = LogManager.getLogger(ActivityServiceImpl.class);
  private ActivityLogDao activityLogDAO;

  public ActivityServiceImpl(ActivityLogDao activityLogDAO) {
    this.activityLogDAO = activityLogDAO;
  }

  @Override
  public Future<List<ActivityLog>> getActivityLogByUserId(UUID userId) {
    return activityLogDAO.getAllActivityLogsByUserId(userId);
  }

  @Override
  public Future<List<ActivityLog>> getAllActivityLogsForAdmin() {
    return activityLogDAO.getAllActivityLogsForAdmin();
  }

  @Override
  public Future<Void> insertActivityLogIntoDb(ActivityLog activityLogEntity) {
    return activityLogDAO.createActivityLog(activityLogEntity).mapEmpty();
  }

  @Override
  public Future<Pagination<ActivityLog>> getAllWitPagination(int limit, int offset) {
    return activityLogDAO
        .getAllWitPagination(limit, offset)
        .compose(
            pagination -> {
              List<ActivityLog> logs = pagination.data();
              return Future.succeededFuture(pagination);
            })
        .recover(
            err -> {
              LOGGER.error("Error fetching paginated activity logs: {}", err.getMessage(), err);
              return Future.failedFuture(err);
            });
  }
}
