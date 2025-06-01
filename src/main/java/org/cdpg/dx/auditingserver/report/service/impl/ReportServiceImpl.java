package org.cdpg.dx.auditingserver.report.service.impl;

import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.streams.ReadStream;
import java.util.UUID;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cdpg.dx.auditingserver.report.dao.ActivityLogDao;
import org.cdpg.dx.auditingserver.report.helper.BatchedCsvReadStream;
import org.cdpg.dx.auditingserver.report.helper.CsvGenerator;
import org.cdpg.dx.auditingserver.report.service.ReportService;
import org.cdpg.dx.common.exception.CsvLimitExceedNoRecordFound;

public class ReportServiceImpl implements ReportService {
  private static final Logger LOGGER = LogManager.getLogger(ReportServiceImpl.class);
  private final ActivityLogDao activityLogDAO;
  private final CsvGenerator csvGenerator;
  private final Vertx vertx;
  private final int limit = 10000; // Default limit for batched CSV generation
  private final int offset = 0; // Default offset for batched CSV generation

  public ReportServiceImpl(ActivityLogDao activityLogDAO, Vertx vertx) {
    this.activityLogDAO = activityLogDAO;
    this.vertx = vertx;
    csvGenerator = new CsvGenerator();
  }

  @Override
  public Future<ReadStream<Buffer>> streamConsumerCsvBatched(UUID userId) {
    LOGGER.info("Inside streamConsumerCsvBatched method");
    Promise<ReadStream<Buffer>> promise = Promise.promise();
    activityLogDAO
        .getCsvGenerateForConsumer(userId, limit, offset)
        .onSuccess(
            result -> {
              LOGGER.debug("total counts :: {}", result.count());
              if (result.count() == 0 || result.count() > 1000000) {
                LOGGER.error("Total count should be in range of (> 0 && <1000000)");
                promise.fail(
                    new CsvLimitExceedNoRecordFound(
                        "No data available for CSV generation/Too many records"));
                return;
              }
              promise.complete(
                  new BatchedCsvReadStream(
                      activityLogDAO, csvGenerator, vertx, limit, result.count(), offset, userId));
            })
        .onFailure(promise::fail);
    return promise.future();
  }

  @Override
  public Future<ReadStream<Buffer>> streamAdminCsvBatched() {
    LOGGER.info("Inside streamAdminCsvBatched method");
    Promise<ReadStream<Buffer>> promise = Promise.promise();
    activityLogDAO
        .getCsvGenerateForAdmin(limit, offset)
        .onSuccess(
            result -> {
              LOGGER.debug("total counts :: {}", result.count());
              if (result.count() == 0 || result.count() > 1000000) {
                LOGGER.error("Total count should be in range of (> 0 && <1000000)");
                promise.fail(
                    new CsvLimitExceedNoRecordFound(
                        "No data available for CSV generation/Too many records"));
                return;
              }
              promise.complete(
                  new BatchedCsvReadStream(
                      activityLogDAO, csvGenerator, vertx, limit, result.count(), offset, null));
            })
        .onFailure(promise::fail);
    return promise.future();
  }
}
