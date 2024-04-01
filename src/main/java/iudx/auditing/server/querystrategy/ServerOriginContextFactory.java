package iudx.auditing.server.querystrategy;

import io.vertx.core.json.JsonObject;

public class ServerOriginContextFactory {
  private final AuditingServerStrategy resourceServerStrategy;
  private final AuditingServerStrategy catalogueServerStrategy;
  private final AuditingServerStrategy authServerStrategy;
  private final AuditingServerStrategy rsSubscriptionStrategy;
  private final AuditingServerStrategy aclApdServerStrategy;
  private final AuditingServerStrategy dmpApdServerStrategy;
  private final AuditingServerStrategy consentLogStrategy;
  private final AuditingServerStrategy ogcServerStrategy;

  public ServerOriginContextFactory(JsonObject config) {
    this.resourceServerStrategy = new ResourceAuditingStrategy(config);
    this.catalogueServerStrategy = new CatalogueAuditingStrategy(config);
    this.authServerStrategy = new AuthAuditingStrategy(config);
    this.rsSubscriptionStrategy = new RsSubscriptionStrategy(config);
    this.aclApdServerStrategy = new AclApdAuditingStrategy(config);
    this.consentLogStrategy = new ConsentLogAuditingStrategy(config);
    this.dmpApdServerStrategy = new DmpApdAuditingStrategy(config);
    this.ogcServerStrategy = new OgcAuditingStrategy(config);
  }

  public AuditingServerStrategy create(ServerOrigin serverOrigin) {

    switch (serverOrigin) {
      case RS_SERVER:
      case DI_SERVER:
      case GIS_SERVER:
      case FILE_SERVER:
        return resourceServerStrategy;
      case CAT_SERVER:
        return catalogueServerStrategy;
      case AAA_SERVER:
        return authServerStrategy;
      case RS_SERVER_SUBS:
        return rsSubscriptionStrategy;
      case ACL_APD_SERVER:
        return aclApdServerStrategy;
      case CONSENT_LOG_ADEX:
        return consentLogStrategy;
      case DMP_APD_SERVER:
        return dmpApdServerStrategy;
      case OGC_SERVER:
        return ogcServerStrategy;
      default:
        throw new IllegalArgumentException(serverOrigin + "serverOrigin is not defined");
    }
  }
}
