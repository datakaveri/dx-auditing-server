package iudx.auditing.server.querystrategy;

import io.vertx.core.json.JsonObject;

public class ServerOriginContextFactory {
  private final AuditingServerStrategy resourceServerStrategy;
  private final AuditingServerStrategy catalogueServerStrategy;
  private final AuditingServerStrategy authServerStrategy;
  private final AuditingServerStrategy rsSubscriptionStrategy;

  public ServerOriginContextFactory(JsonObject config) {
    this.resourceServerStrategy = new ResourceAuditingStrategy(config);
    this.catalogueServerStrategy = new CatalogueAuditingStrategy(config);
    this.authServerStrategy = new AuthAuditingStrategy(config);
    this.rsSubscriptionStrategy = new RsSubscriptionStrategy(config);
  }

  public AuditingServerStrategy create(ServerOrigin serverOrigin) {

    switch (serverOrigin) {
      case RS_SERVER:
      case DI_SERVER:
      case GIS_SERVER:
      case FILE_SERVER:
        {
          return resourceServerStrategy;
        }
      case CAT_SERVER:
        {
          return catalogueServerStrategy;
        }
      case AAA_SERVER:
        {
          return authServerStrategy;
        }
      case RS_SERVER_SUBS:
        {
          return rsSubscriptionStrategy;
        }
      default:
        throw new IllegalArgumentException(serverOrigin + "serverOrigin is not defined");
    }
  }
}
