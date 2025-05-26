package org.cdpg.dx.auditingserver.report.service.impl;

import static org.cdpg.dx.auditingserver.report.util.Constants.EMPTY_FILE;
import static org.cdpg.dx.auditingserver.report.util.Constants.TOO_MANY_ROWS;

import io.vertx.core.Future;
import io.vertx.core.Promise;
import java.util.UUID;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cdpg.dx.auditingserver.report.dao.ActivityLogDao;
import org.cdpg.dx.auditingserver.report.helper.CsvGenerator;
import org.cdpg.dx.auditingserver.report.service.ReportService;

public class ReportServiceImpl implements ReportService {
  private static final Logger LOGGER = LogManager.getLogger(ReportServiceImpl.class);
  private final ActivityLogDao activityLogDAO;
  private final CsvGenerator csvGenerator;

  public ReportServiceImpl(ActivityLogDao activityLogDAO) {
    this.activityLogDAO = activityLogDAO;
    csvGenerator = new CsvGenerator();
  }

  @Override
  public Future<String> getAdminCsvReport() {
    Promise<String> promise = Promise.promise();

    activityLogDAO
        .getCsvGenerateForAdmin()
        .compose(
            activityLogs -> {
              return csvGenerator.toCsv(activityLogs);
            })
        .onSuccess(
            csvSuccess -> {
              LOGGER.info("response :: {}", csvSuccess);
              if (EMPTY_FILE.equalsIgnoreCase(csvSuccess)) {
                promise.complete(EMPTY_FILE);
              } else if (TOO_MANY_ROWS.equalsIgnoreCase(csvSuccess)) {
                promise.complete(TOO_MANY_ROWS);
              } else {
                promise.complete(csvSuccess);
              }
            })
        .onFailure(promise::fail);
    return promise.future();
  }

  @Override
  public Future<String> getConsumerCsvReport(UUID userId) {
    Promise<String> promise = Promise.promise();
    activityLogDAO
        .getCsvGeneratedByUserId(userId)
        .compose(activityLogs -> csvGenerator.toCsv(activityLogs))
        .onSuccess(
            csvSuccess -> {
              LOGGER.info("response :: {}", csvSuccess);
              if (EMPTY_FILE.equalsIgnoreCase(csvSuccess)) {
                promise.complete(EMPTY_FILE);
              } else if (TOO_MANY_ROWS.equalsIgnoreCase(csvSuccess)) {
                promise.complete(TOO_MANY_ROWS);
              } else {
                promise.complete(csvSuccess);
              }
            })
        .onFailure(promise::fail);
    return promise.future();
  }
}
