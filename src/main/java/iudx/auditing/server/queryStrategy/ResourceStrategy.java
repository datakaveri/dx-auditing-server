package iudx.auditing.server.queryStrategy;

import io.vertx.core.json.JsonObject;

public class ResourceStrategy implements ServerStrategy{
  @Override
  public String buildWriteQuery(JsonObject request) {
    return null;
  }
}
