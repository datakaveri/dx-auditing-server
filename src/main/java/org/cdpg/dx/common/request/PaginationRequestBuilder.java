package org.cdpg.dx.common.request;

import io.vertx.ext.web.RoutingContext;
import java.util.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cdpg.dx.common.exception.DxBadRequestException;

public class PaginationRequestBuilder {
  private static final Logger LOGGER = LogManager.getLogger(PaginationRequestBuilder.class);

  /**
   * Extracts a paginated request from the routing context, applying filters and temporal requests.
   *
   * @param ctx The routing context containing query parameters.
   * @param allowedFilterKeys Set of allowed filter keys to extract from the query parameters.
   * @param apiToDbMap Mapping from API filter keys to database column names.
   * @param allowedTimeFields The field representing the time in the request (e.g., "created_at").
   * @return A PaginatedRequest object containing pagination, filters, and temporal requests.
   */
  public static PaginatedRequest builder(
      RoutingContext ctx,
      Set<String> allowedFilterKeys,
      Map<String, String> apiToDbMap,
      Map<String, String> additionalFilters,
      Set<String> allowedTimeFields,
      String defaultTimeField) {
    int page = parseIntOrDefault(ctx.queryParam("page"), 1);
    int size = parseIntOrDefault(ctx.queryParam("size"), 1000);

    Map<String, String> rawFilters = new HashMap<>();
    for (String key : allowedFilterKeys) {
      ctx.queryParam(key).stream().findFirst().ifPresent(value -> rawFilters.put(key, value));
    }

    Map<String, String> mappedFilters = FilterMapper.mapFilters(rawFilters, apiToDbMap);
    if (additionalFilters != null) {
      mappedFilters.putAll(additionalFilters);
    }

    List<TemporalRequest> temporalRequests = new ArrayList<>();

    // Handle default time params (no prefix)
    String time = ctx.queryParam("time").stream().findFirst().orElse(null);
    String endtime = ctx.queryParam("endtime").stream().findFirst().orElse(null);
    String timeRel = ctx.queryParam("timerel").stream().findFirst().orElse(null);

    if (endtime != null && time == null) {
      throw new DxBadRequestException("Parameter 'endtime' cannot be used without 'time'.");
    }
    if (time != null && timeRel == null) {
      throw new DxBadRequestException(
          "Parameter 'timerel' is required when 'temporal query ' is provided.");
    }
    if (timeRel != null) {
      TemporalRequest tr =
          TemporalRequestHelper.buildTemporalRequest(defaultTimeField, timeRel, time, endtime);
      if (tr != null) temporalRequests.add(tr);
    }

    // Handle additional time fields
    for (String timeField : allowedTimeFields) {
      String t = ctx.queryParam(timeField + "_time").stream().findFirst().orElse(null);
      String et = ctx.queryParam(timeField + "_endtime").stream().findFirst().orElse(null);
      String trl = ctx.queryParam(timeField + "_timerel").stream().findFirst().orElse(null);
      if (trl != null) {
        TemporalRequest tr = TemporalRequestHelper.buildTemporalRequest(timeField, trl, t, et);
        if (tr != null) temporalRequests.add(tr);
      }
    }

    LOGGER.debug("Extracted temporal requests: {}", temporalRequests);
    return new PaginatedRequest(page, size, mappedFilters, temporalRequests);
  }

  private static int parseIntOrDefault(List<String> values, int defaultValue) {
    if (values == null || values.isEmpty()) return defaultValue;
    try {
      return Integer.parseInt(values.get(0));
    } catch (NumberFormatException e) {
      return defaultValue;
    }
  }
}
