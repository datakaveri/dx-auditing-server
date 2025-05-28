package org.cdpg.dx.auditingserver.activity.dao;

import io.vertx.core.Future;
import java.util.UUID;
import org.cdpg.dx.auditingserver.activity.model.ActivityLog;
import org.cdpg.dx.auditingserver.activity.model.ActivityLogAdminRequest;
import org.cdpg.dx.auditingserver.activity.model.Pagination;
import org.cdpg.dx.database.postgres.base.dao.BaseDAO;

public interface ActivityLogDao extends BaseDAO<ActivityLog> {
  // Add any custom DAO methods if needed later

  Future<Pagination<ActivityLog>> getAllActivityLogsByUserId(UUID userId, int limit, int offset);

  Future<ActivityLog> createActivityLog(ActivityLog activityLogEntity);

  Future<Pagination<ActivityLog>> getAllActivityLogsForAdmin(
      ActivityLogAdminRequest activityLogAdminRequest);

  Future<Pagination<ActivityLog>> getAllWitPagination(int limit, int offset);
}
