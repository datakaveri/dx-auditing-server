package org.cdpg.dx.auditing.activity.service;

import io.vertx.core.Future;
import java.util.List;
import java.util.UUID;
import org.cdpg.dx.auditing.activity.dao.ActivityLogDAO;
import org.cdpg.dx.auditing.activity.model.ActivityLog;

public class ActivityServiceImpl implements ActivityService {
  private ActivityLogDAO activityLogDAO;

  public ActivityServiceImpl(ActivityLogDAO activityLogDAO) {
    this.activityLogDAO = activityLogDAO;
  }

  @Override
  public Future<List<ActivityLog>> getActivityLogByUserId(UUID userId) {
    return activityLogDAO.getAllActivityLogsByUserId(userId);
  }

  @Override
  public Future<Void> createActivityLog(ActivityLog activityLog) {
    return activityLogDAO.createActivityLog(activityLog);
  }
}
