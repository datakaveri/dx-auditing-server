package org.cdpg.dx.auditing.apiserver;

import static org.cdpg.dx.common.util.ProxyAddressConstants.*;
// import org.cdpg.dx.databroker.service.DataBrokerService;

import io.vertx.core.Vertx;
import java.util.List;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cdpg.dx.database.postgres.service.PostgresService;

public class ControllerFactory {
  private static final Logger LOGGER = LogManager.getLogger(ControllerFactory.class);
  private final Vertx vertx;
  private PostgresService pgService;

  public ControllerFactory(Vertx vertx) {
    this.vertx = vertx;
    CreateProxies(vertx);
  }

  public List<ApiController> createControllers() {
    return List.of(new ActivityController(pgService, vertx));
  }

  private void CreateProxies(Vertx vertx) {
    pgService = PostgresService.createProxy(vertx, PG_SERVICE_ADDRESS);
  }
}
