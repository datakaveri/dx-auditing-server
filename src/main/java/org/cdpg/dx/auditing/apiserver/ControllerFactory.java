package org.cdpg.dx.auditing.apiserver;

import static org.cdpg.dx.common.util.ProxyAddressConstants.PG_SERVICE_ADDRESS;

import io.vertx.core.Vertx;
import java.util.List;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cdpg.dx.database.postgres.service.PostgresService;
//import org.cdpg.dx.databroker.service.DataBrokerService;

public class ControllerFactory {
  private static final Logger LOGGER = LogManager.getLogger(ControllerFactory.class);
  private final Vertx vertx;
  private PostgresService pgService;

 // private DataBrokerService brokerService;
  private String dxApiBasePath;

  public ControllerFactory(String dxApiBasePath, Vertx vertx) {
    this.dxApiBasePath = dxApiBasePath;
    this.vertx = vertx;
    CreateProxies(vertx);
  }

  public List<ApiController> createControllers() {
    return null;
  }

  private void CreateProxies(Vertx vertx) {
    pgService = PostgresService.createProxy(vertx, PG_SERVICE_ADDRESS);
   // brokerService = DataBrokerService.createProxy(vertx, DATA_BROKER_SERVICE_ADDRESS);
  }
}
