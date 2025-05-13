package org.cdpg.dx.auditingserver.activity.dao;

import io.vertx.core.Future;
import java.util.List;
import java.util.UUID;
import org.cdpg.dx.auditingserver.activity.model.ActivityLogEntity;
import org.cdpg.dx.database.postgres.base.dao.BaseDAO;

public interface ActivityLogDao extends BaseDAO<ActivityLogEntity> {
  // Add any custom DAO methods if needed later

  Future<List<ActivityLogEntity>> getAllActivityLogsByUserId(UUID userId);

  Future<ActivityLogEntity> createActivityLog(ActivityLogEntity activityLogEntity);
}
