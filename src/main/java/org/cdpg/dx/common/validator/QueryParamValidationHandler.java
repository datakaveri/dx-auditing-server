package org.cdpg.dx.common.validator;

import io.vertx.core.Handler;
import io.vertx.ext.web.RoutingContext;
import java.util.Set;
import java.util.stream.Collectors;
import org.cdpg.dx.common.exception.DxQueryParamValidationException;

public class QueryParamValidationHandler implements Handler<RoutingContext> {
  private final Set<String> allowedParams;

  public QueryParamValidationHandler(Set<String> allowedParams) {
    this.allowedParams = allowedParams;
  }

  /**
   * Validates query parameters in the RoutingContext against a set of allowed parameters. If any
   * unknown parameters are found, it fails the context with a DxQueryParamValidationException.
   *
   * @param ctx the RoutingContext containing the query parameters to validate
   */
  @Override
  public void handle(RoutingContext ctx) {
    Set<String> unknownParams =
        ctx.queryParams().names().stream()
            .filter(param -> !allowedParams.contains(param))
            .collect(Collectors.toSet());
    if (!unknownParams.isEmpty()) {
      ctx.fail(
          new DxQueryParamValidationException(
              "Invalid query parameters: " + String.join(", ", unknownParams)));
      return;
    }
    ctx.next();
  }
}
