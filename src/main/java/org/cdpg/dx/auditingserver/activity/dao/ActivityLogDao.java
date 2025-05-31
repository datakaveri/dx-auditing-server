package org.cdpg.dx.auditingserver.activity.dao;

import io.vertx.core.Future;
import org.cdpg.dx.auditingserver.activity.model.ActivityLog;
import org.cdpg.dx.auditingserver.activity.model.ActivityLogRequest;
import org.cdpg.dx.database.postgres.models.PagedResult;
import org.cdpg.dx.database.postgres.base.dao.BaseDAO;

public interface ActivityLogDao extends BaseDAO<ActivityLog> {
  // Add any custom DAO methods if needed later

  Future<PagedResult<ActivityLog>> getAllActivityLogsByUserId(ActivityLogRequest activityLogRequest);

  Future<ActivityLog> createActivityLog(ActivityLog activityLogEntity);

  Future<PagedResult<ActivityLog>> getAllActivityLogsForAdmin(
      ActivityLogRequest activityLogAdminRequest);
}
