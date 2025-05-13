package org.cdpg.dx.auditingserver.activity.service.impl;

import io.vertx.core.Future;
import java.util.List;
import java.util.UUID;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cdpg.dx.auditingserver.activity.dao.ActivityLogDao;
import org.cdpg.dx.auditingserver.activity.model.ActivityLogEntity;
import org.cdpg.dx.auditingserver.activity.service.ActivityService;

public class ActivityServiceImpl implements ActivityService {
  private static final Logger LOGGER = LogManager.getLogger(ActivityServiceImpl.class);
  private ActivityLogDao activityLogDAO;

  public ActivityServiceImpl(ActivityLogDao activityLogDAO) {
    this.activityLogDAO = activityLogDAO;
  }

  @Override
  public Future<List<ActivityLogEntity>> getActivityLogByUserId(UUID userId) {
    return activityLogDAO.getAllActivityLogsByUserId(userId);
  }

  @Override
  public Future<Void> insertActivityLogIntoDb(ActivityLogEntity activityLogEntity) {
    LOGGER.trace("insertActivityLogIntoDb() started");
    return activityLogDAO.createActivityLog(activityLogEntity).mapEmpty();
  }
}
