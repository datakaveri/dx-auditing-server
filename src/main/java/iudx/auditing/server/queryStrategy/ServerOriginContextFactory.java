package iudx.auditing.server.queryStrategy;

import io.vertx.core.json.JsonObject;

public class ServerOriginContextFactory {
  private final ServerStrategy resourceServerStrategy;
  private final ServerStrategy catalogueServerStrategy;
  private final ServerStrategy authServerStrategy;

  public ServerOriginContextFactory(JsonObject config) {
    this.resourceServerStrategy = new ResourceStrategy(config);
    this.catalogueServerStrategy = new CatalogueStrategy(config);
    this.authServerStrategy = new AuthStrategy(config);
  }

  public ServerStrategy create(ServerOrigin serverOrigin) {
    switch (serverOrigin) {
      case RS_SERVER:
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
      default:
        throw new IllegalArgumentException(serverOrigin + "serverOrigin is not defined");
    }
  }
}
