package org.cdpg.dx.auditing.activity.dao;

import io.vertx.core.Future;
import java.util.List;
import java.util.UUID;
import org.cdpg.dx.auditing.activity.model.ActivityLog;
import org.cdpg.dx.database.postgres.base.dao.BaseDAO;

public interface ActivityLogDao extends BaseDAO<ActivityLog> {
  // Add any custom DAO methods if needed later

  Future<List<ActivityLog>> getAllActivityLogsByUserId(UUID userId);

  Future<Void> createActivityLog(ActivityLog activityLog);
}
