package org.cdpg.dx.common;

import static org.cdpg.dx.common.config.CorsUtil.allowedOrigins;
import static org.cdpg.dx.util.Constants.HEADER_ALLOW_ORIGIN;

import io.vertx.core.Handler;
import io.vertx.ext.web.RoutingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cdpg.dx.common.config.CorsUtil;
import org.cdpg.dx.common.response.DxErrorResponse;
import org.cdpg.dx.common.util.ExceptionHttpStatusMapper;
import org.cdpg.dx.common.util.ThrowableUtils;

public class FailureHandler implements Handler<RoutingContext> {

  private static final Logger LOGGER = LogManager.getLogger(FailureHandler.class);

  public void handle(RoutingContext context) {
    Throwable failure = context.failure();
    LOGGER.info("FailureHandler: {}", failure.getClass());
    if (failure == null) {
      failure = new RuntimeException("Unknown server error");
    }

    HttpStatusCode statusCode = ExceptionHttpStatusMapper.map(failure);

    // Log complete error with stack trace for diagnostics
    LOGGER.error("Unhandled error: {}", failure.getMessage(), failure);

    // Avoid leaking internal exception messages
    String safeDetail =
        ThrowableUtils.isSafeToExpose(failure)
            ? failure.getMessage()
            : "An unexpected error occurred";

    DxErrorResponse errorResponse =
        new DxErrorResponse(statusCode.getUrn(), statusCode.getDescription(), safeDetail);

    if (!context.response().ended()) {
      int status = statusCode.getValue();
      if (status < 400 || status > 599) {
        status = 500;
      }

      String requestOrigin = context.request().getHeader("Origin");
      if (allowedOrigins != null && requestOrigin != null && allowedOrigins.contains(requestOrigin)
          || allowedOrigins.contains("*")) {
        context
            .response()
            .putHeader("Content-Type", "application/json")
            .putHeader(HEADER_ALLOW_ORIGIN, requestOrigin)
            .putHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS")
            .putHeader("Access-Control-Allow-Headers", "Authorization, Content-Type")
            .setStatusCode(status)
            .end(errorResponse.toJson().encode());
      } else {
        context
            .response()
            .putHeader("Content-Type", "application/json")
            .setStatusCode(status)
            .end(errorResponse.toJson().encode());
      }
    }
  }
}
