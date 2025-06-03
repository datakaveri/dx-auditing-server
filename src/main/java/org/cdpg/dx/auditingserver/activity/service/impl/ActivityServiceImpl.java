package org.cdpg.dx.auditingserver.activity.service.impl;

import io.vertx.core.Future;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cdpg.dx.auditingserver.activity.dao.ActivityLogDao;
import org.cdpg.dx.auditingserver.activity.model.ActivityLog;
import org.cdpg.dx.auditingserver.activity.model.ActivityLogRequest;
import org.cdpg.dx.auditingserver.activity.service.ActivityService;
import org.cdpg.dx.common.request.PaginatedRequest;
import org.cdpg.dx.database.postgres.models.PagedResult;

public class ActivityServiceImpl implements ActivityService {
  private static final Logger LOGGER = LogManager.getLogger(ActivityServiceImpl.class);
  private final ActivityLogDao activityLogDAO;

  public ActivityServiceImpl(ActivityLogDao activityLogDAO) {
    this.activityLogDAO = activityLogDAO;
  }

  @Override
  public Future<PagedResult<ActivityLog>> getActivityLogByUserId(
      ActivityLogRequest activityLogRequest) {
    return activityLogDAO.getAllActivityLogsByUserId(activityLogRequest);
  }

  @Override
  public Future<PagedResult<ActivityLog>> getAllActivityLogsForAdmin(
      PaginatedRequest paginatedRequest) {
    return activityLogDAO.getAllWithFilters(paginatedRequest);
  }

  @Override
  public Future<Void> insertActivityLogIntoDb(ActivityLog activityLogEntity) {
    return activityLogDAO.createActivityLog(activityLogEntity).mapEmpty();
  }

  @Override
  public Future<PagedResult<ActivityLog>> getActivityLogForConsumer(
      PaginatedRequest paginatedRequest) {
    return activityLogDAO.getAllWithFilters(paginatedRequest);
  }
}
