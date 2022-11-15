package iudx.auditing.server.processor;

import io.vertx.core.json.JsonObject;

interface IProcessor {
  void process(JsonObject json);
}
