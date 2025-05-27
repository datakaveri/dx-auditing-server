package org.cdpg.dx.auditingserver.report.service;

import io.vertx.core.Future;
import java.util.UUID;

public interface ReportService {
  Future<String> getAdminCsvReport();

  Future<String> getConsumerCsvReport(UUID userId);
}
