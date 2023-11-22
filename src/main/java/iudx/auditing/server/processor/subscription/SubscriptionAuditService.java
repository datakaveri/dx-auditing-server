package iudx.auditing.server.processor.subscription;

import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;

public interface SubscriptionAuditService {

  Future<Void> generateAuditLog(JsonObject consumedMessage);
}
