package org.cdpg.dx.auditingserver.report.model;

import java.util.List;

public record ReportMetaData(List<ActivityLog> activityLogList, long count) {}
