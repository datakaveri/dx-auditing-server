package org.cdpg.dx.common.request;

import java.util.List;
import java.util.Map;

public record PaginatedRequest(
    int page, int size, Map<String, String> filters, List<TemporalRequest> temporalRequests) {}
