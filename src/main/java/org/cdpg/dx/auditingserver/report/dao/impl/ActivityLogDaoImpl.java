package org.cdpg.dx.auditingserver.report.dao.impl;

import static org.cdpg.dx.auditingserver.report.util.ActivityConstants.*;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cdpg.dx.auditingserver.report.dao.ActivityLogDao;
import org.cdpg.dx.auditingserver.report.model.ActivityLog;
import org.cdpg.dx.database.postgres.base.dao.AbstractBaseDAO;
import org.cdpg.dx.database.postgres.service.PostgresService;

public class ActivityLogDaoImpl extends AbstractBaseDAO<ActivityLog> implements ActivityLogDao {
  private static final Logger LOGGER = LogManager.getLogger(ActivityLogDaoImpl.class);

  public ActivityLogDaoImpl(PostgresService postgresService) {
    super(postgresService, ACTIVITY_LOG_TABLE_NAME, ID, ActivityLog::fromJson);
  }
}
