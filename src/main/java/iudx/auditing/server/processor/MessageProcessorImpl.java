package iudx.auditing.server.processor;

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import iudx.auditing.server.rabbitmq.RabbitMQService;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.codehaus.jackson.annotate.JsonProperty;

public class MessageProcessorImpl implements MessageProcessService {

  private static final Logger LOGGER = LogManager.getLogger(MessageProcessorImpl.class);
  private final Vertx vertx;
  private final RabbitMQService rabbitMQService;

  public MessageProcessorImpl(Vertx vertx, RabbitMQService rabbitMQService) {
    this.vertx = vertx;
    this.rabbitMQService = rabbitMQService;
  }

  @Override
  public MessageProcessService process(JsonObject message,
      Handler<AsyncResult<JsonObject>> handler) {
    LOGGER.info("message processing starts : ");
    if (message == null || message.isEmpty()) {
      handler.handle(Future.failedFuture("empty/null message received"));
    } else {
      // code for immudb and postgres
    }
    return this;
  }

}
