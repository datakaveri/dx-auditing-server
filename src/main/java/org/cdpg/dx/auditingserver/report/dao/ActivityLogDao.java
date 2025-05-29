package org.cdpg.dx.auditingserver.report.dao;

import io.vertx.core.Future;
import java.util.List;
import java.util.UUID;
import org.cdpg.dx.auditingserver.report.model.ActivityLog;
import org.cdpg.dx.auditingserver.report.model.ReportMetaData;
import org.cdpg.dx.database.postgres.base.dao.BaseDAO;

public interface ActivityLogDao extends BaseDAO<ActivityLog> {

  Future<ReportMetaData> getCsvGenerateForAdmin(int limit, int offset);

  Future<ReportMetaData> getCsvGenerateForConsumer(UUID userId, int limit, int offset);

  Future<List<ActivityLog>> getCsvGeneratedByPaginated(int limit, int offset, String userId);
}
