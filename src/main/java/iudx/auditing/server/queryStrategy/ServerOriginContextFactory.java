package iudx.auditing.server.queryStrategy;

public class ServerOriginContextFactory {
  private final ServerStrategy resourceServerStrategy;
  private final ServerStrategy catalogueServerStrategy;
  private final ServerStrategy authServerStrategy;
  private final ServerStrategy fileServerStrategy;
  private final ServerStrategy gisServerStrategy;
  public ServerOriginContextFactory() {
    this.resourceServerStrategy = new ResourceStrategy();
    this.fileServerStrategy= new FileStrategy();
    this.gisServerStrategy= new GisStrategy();
    this.catalogueServerStrategy= new CatalogueStrategy();
    this.authServerStrategy= new AuthStrategy();
  }

  public ServerStrategy create(ServerOrigin serverOrigin) {
    switch (serverOrigin) {
      case RS_SERVER: {
        return resourceServerStrategy;
      }
      case CAT_SERVER: {
        return catalogueServerStrategy;
      }
      case AAA_SERVER: {
        return authServerStrategy;
      }
      case GIS_SERVER:{
        return gisServerStrategy;
      }
      case FILE_SERVER:{
        return fileServerStrategy;
      }
      default:
        throw new IllegalArgumentException(serverOrigin + "serverOrigin is not defined");
    }
  }

}

