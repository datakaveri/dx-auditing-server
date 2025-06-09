package org.cdpg.dx.auditingserver.activity.util;

import java.util.Map;
import java.util.Set;

public final class ActivityConstants {
  // Table names
  public static final String ACTIVITY_LOG_TABLE_NAME = "user_activity_log";
  // Common fields
  public static final String ID = "id";
  public static final String USER_ID = "user_id";
  public static final String NAME = "name";
  public static final String DESCRIPTION = "description";
  public static final String SHORT_DESCRIPTION = "short_description";
  public static final String CREATED_AT = "created_at";
  public static final String UPDATED_AT = "updated_at";
  public static final String ASSET_NAME = "asset_name";

  // Asset activity log fields
  public static final String ASSET_TYPE = "asset_type";
  public static final String OPERATION = "operation";
  public static final String ASSET_ID = "asset_id";
  public static final String API = "api";
  public static final String METHOD = "method";
  public static final String SIZE = "size";
  public static final String ROLE = "role";
  public static final String ORIGIN_SERVER = "origin_server";
  public static final String MYACTIVITY_ENABLED = "myactivity_enabled";
  public static final Map<String, String> API_TO_DB_MAP =
      Map.of(
          "userId",
          "user_id",
          "assetType",
          "asset_type",
          "operation",
          "operation",
          "createdAt",
          "created_at",
          "assetId",
          "asset_id",
          "assetName",
          "asset_name",
          "role",
          "role",
          "api",
          "api");
  public static Set<String> allowedQueryParamsForConsumer =
      Set.of("assetType", "operation", "time", "endtime", "timerel", "page", "size", "sort");
  public static Set<String> allowedQueryParamsForAdmin =
      Set.of(
          "userId", "assetType", "operation", "time", "endtime", "timerel", "page", "size", "sort");

  private ActivityConstants() {
    // Utility class; prevent instantiation
  }
}
