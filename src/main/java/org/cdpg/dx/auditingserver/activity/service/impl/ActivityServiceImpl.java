package org.cdpg.dx.auditingserver.activity.service.impl;

import io.vertx.core.Future;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cdpg.dx.auditingserver.activity.dao.ActivityLogDao;
import org.cdpg.dx.auditingserver.activity.model.ActivityLog;
import org.cdpg.dx.auditingserver.activity.model.ActivityLogRequest;
import org.cdpg.dx.auditingserver.activity.model.Pagination;
import org.cdpg.dx.auditingserver.activity.service.ActivityService;

public class ActivityServiceImpl implements ActivityService {
  private static final Logger LOGGER = LogManager.getLogger(ActivityServiceImpl.class);
  private final ActivityLogDao activityLogDAO;

  public ActivityServiceImpl(ActivityLogDao activityLogDAO) {
    this.activityLogDAO = activityLogDAO;
  }

  @Override
  public Future<Pagination<ActivityLog>> getActivityLogByUserId(
      ActivityLogRequest activityLogRequest) {
    return activityLogDAO.getAllActivityLogsByUserId(activityLogRequest);
  }

  @Override
  public Future<Pagination<ActivityLog>> getAllActivityLogsForAdmin(
      ActivityLogRequest activityLogAdminRequest) {
    return activityLogDAO.getAllActivityLogsForAdmin(activityLogAdminRequest);
  }

  @Override
  public Future<Void> insertActivityLogIntoDb(ActivityLog activityLogEntity) {
    return activityLogDAO.createActivityLog(activityLogEntity).mapEmpty();
  }
}
