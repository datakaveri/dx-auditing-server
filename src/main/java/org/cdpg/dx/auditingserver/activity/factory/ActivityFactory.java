package org.cdpg.dx.auditingserver.activity.factory;

import org.cdpg.dx.auditingserver.activity.dao.ActivityLogDao;
import org.cdpg.dx.auditingserver.activity.dao.impl.ActivityLogDaoImpl;
import org.cdpg.dx.auditingserver.activity.service.ActivityService;
import org.cdpg.dx.auditingserver.activity.service.impl.ActivityServiceImpl;
import org.cdpg.dx.database.postgres.service.PostgresService;

public class ActivityFactory {

  public static ActivityService create(PostgresService postgresService) {
    ActivityLogDao activityLogDao = new ActivityLogDaoImpl(postgresService);

    return new ActivityServiceImpl(activityLogDao);
  }
}
