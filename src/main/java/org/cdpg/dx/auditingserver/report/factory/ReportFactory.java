package org.cdpg.dx.auditingserver.report.factory;

import io.vertx.core.Vertx;
import org.cdpg.dx.auditingserver.report.dao.ActivityLogDao;
import org.cdpg.dx.auditingserver.report.dao.impl.ActivityLogDaoImpl;
import org.cdpg.dx.auditingserver.report.service.ReportService;
import org.cdpg.dx.auditingserver.report.service.impl.ReportServiceImpl;
import org.cdpg.dx.database.postgres.service.PostgresService;

public class ReportFactory {
  public static ReportService create(PostgresService pgService, Vertx vertx) {
    ActivityLogDao activityLogDao = new ActivityLogDaoImpl(pgService);
    return new ReportServiceImpl(activityLogDao, vertx);
  }
}