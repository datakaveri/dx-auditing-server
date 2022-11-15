package iudx.auditing.server.queryStrategy;

import java.util.stream.Stream;

public enum ServerOrigin {
  RS_SERVER("rs-server"),
  CAT_SERVER("cat-server"),
  AAA_SERVER("auth-server"),
  FILE_SERVER("file-server"),
  GIS_SERVER("gis-server");

  private final String originRole;

  ServerOrigin(String originRole) {
    this.originRole = originRole;
  }

  public String getOriginRole() {
    return this.originRole;
  }
  public static ServerOrigin fromRole(final String originRole){
    return Stream.of(values())
      .filter(v -> v.originRole.equalsIgnoreCase(originRole))
      .findAny()
      .orElse(null);
  }
}
