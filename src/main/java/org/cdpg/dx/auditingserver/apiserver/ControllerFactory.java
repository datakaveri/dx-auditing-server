package org.cdpg.dx.auditingserver.apiserver;

import static org.cdpg.dx.common.config.ServiceProxyAddressConstants.*;

import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import java.util.List;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cdpg.dx.auditingserver.activity.controller.ActivityController;
import org.cdpg.dx.auditingserver.activity.factory.ActivityFactory;
import org.cdpg.dx.auditingserver.activity.service.ActivityService;
import org.cdpg.dx.auditingserver.report.controller.ReportController;
import org.cdpg.dx.auditingserver.report.factory.ReportFactory;
import org.cdpg.dx.auditingserver.report.service.ReportService;
import org.cdpg.dx.database.postgres.service.PostgresService;

public class ControllerFactory {
  private static final Logger LOGGER = LogManager.getLogger(ControllerFactory.class);

  private ControllerFactory() {}

  public static List<ApiController> createControllers(Vertx vertx, JsonObject config) {
    PostgresService pgService = PostgresService.createProxy(vertx, POSTGRES_SERVICE_ADDRESS);

    ActivityService activityService = ActivityFactory.create(pgService);
    ReportService reportService = ReportFactory.create(pgService, vertx);

    return List.of(new ActivityController(activityService), new ReportController(reportService));
  }
}