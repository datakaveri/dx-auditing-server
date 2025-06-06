package org.cdpg.dx.auditingserver.report.service.impl;

import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.streams.ReadStream;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cdpg.dx.auditingserver.report.dao.ActivityLogDao;
import org.cdpg.dx.auditingserver.report.helper.BatchedCsvReadStream;
import org.cdpg.dx.auditingserver.report.helper.CsvGenerator;
import org.cdpg.dx.auditingserver.report.service.ReportService;
import org.cdpg.dx.common.request.PaginatedRequest;

public class ReportServiceImpl implements ReportService {
  private static final Logger LOGGER = LogManager.getLogger(ReportServiceImpl.class);
  private final ActivityLogDao activityLogDAO;
  private final CsvGenerator csvGenerator;
  private final Vertx vertx;

  public ReportServiceImpl(ActivityLogDao activityLogDAO, Vertx vertx) {
    this.activityLogDAO = activityLogDAO;
    this.vertx = vertx;
    csvGenerator = new CsvGenerator();
  }

  @Override
  public Future<ReadStream<Buffer>> streamConsumerCsvBatched(PaginatedRequest request) {
    LOGGER.info("Inside streamConsumerCsvBatched method");
    return Future.succeededFuture(
        new BatchedCsvReadStream(activityLogDAO, csvGenerator, vertx, request));
  }

  @Override
  public Future<ReadStream<Buffer>> streamAdminCsvBatched(PaginatedRequest request) {
    LOGGER.info("Inside streamAdminCsvBatched method");
    return Future.succeededFuture(
        new BatchedCsvReadStream(activityLogDAO, csvGenerator, vertx, request));
  }
}
