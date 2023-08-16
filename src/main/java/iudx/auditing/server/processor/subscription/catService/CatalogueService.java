package iudx.auditing.server.processor.subscription.catService;

import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;

public interface CatalogueService {
  Future<JsonObject> searchCatItem(String id);
}
