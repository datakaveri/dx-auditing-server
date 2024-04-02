package iudx.auditing.server.querystrategy;

import java.util.stream.Stream;

public enum ServerOrigin {
  RS_SERVER("rs-server"),
  CAT_SERVER("cat-server"),
  AAA_SERVER("auth-server"),
  FILE_SERVER("file-server"),
  GIS_SERVER("gis-server"),
  DI_SERVER("data-ingestion"),
  RS_SERVER_SUBS("rs-server-subscriptions"),
  ACL_APD_SERVER("acl-apd-server"),
  DMP_APD_SERVER("dmp-apd-server"),
  CONSENT_LOG_ADEX("consent-log"),
  OGC_SERVER("ogc-server");
  private final String originRole;

  ServerOrigin(String originRole) {
    this.originRole = originRole;
  }

  public static ServerOrigin fromRole(final String originRole) {
    return Stream.of(values())
        .filter(v -> v.originRole.equalsIgnoreCase(originRole))
        .findAny()
        .orElse(null);
  }

  public String getOriginRole() {
    return this.originRole;
  }
}
