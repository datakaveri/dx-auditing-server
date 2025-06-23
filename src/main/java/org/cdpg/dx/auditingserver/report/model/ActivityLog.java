package org.cdpg.dx.auditingserver.report.model;

import static org.cdpg.dx.auditingserver.report.util.ActivityConstants.ACTIVITY_LOG_TABLE_NAME;

import io.vertx.core.json.JsonObject;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.cdpg.dx.auditingserver.report.util.ActivityConstants;
import org.cdpg.dx.common.util.DateTimeHelper;
import org.cdpg.dx.database.postgres.base.entity.BaseEntity;
import org.cdpg.dx.database.postgres.util.EntityUtil;

public record ActivityLog(
    UUID id,
    String assetName,
    String assetType,
    String operation,
    LocalDateTime createdAt,
    UUID assetId,
    String api,
    String method,
    Long size,
    String role,
    UUID userId,
    String originServer,
    Boolean myactivityEnabled)
    implements BaseEntity<ActivityLog> {

  public static ActivityLog fromJson(JsonObject json) {
    return new ActivityLog(
        EntityUtil.parseUUID(json.getString(ActivityConstants.ID), ActivityConstants.ID),
        json.getString(ActivityConstants.ASSET_NAME),
        json.getString(ActivityConstants.ASSET_TYPE),
        json.getString(ActivityConstants.OPERATION),
        DateTimeHelper.parse(json.getString(ActivityConstants.CREATED_AT)),
        EntityUtil.parseUUID(
            json.getString(ActivityConstants.ASSET_ID), ActivityConstants.ASSET_ID),
        json.getString(ActivityConstants.API),
        json.getString(ActivityConstants.METHOD),
        json.getLong(ActivityConstants.SIZE),
        json.getString(ActivityConstants.ROLE),
        EntityUtil.parseUUID(json.getString(ActivityConstants.USER_ID), ActivityConstants.USER_ID),
        json.getString(ActivityConstants.ORIGIN_SERVER),
        json.getBoolean(ActivityConstants.MYACTIVITY_ENABLED, null));
  }

  @Override
  public Map<String, Object> toNonEmptyFieldsMap() {
    Map<String, Object> map = new HashMap<>();
    EntityUtil.putIfNonEmpty(map, ActivityConstants.ID, id.toString());
    EntityUtil.putIfNonEmpty(map, ActivityConstants.ASSET_NAME, assetName);
    EntityUtil.putIfNonEmpty(map, ActivityConstants.ASSET_TYPE, assetType);
    EntityUtil.putIfNonEmpty(map, ActivityConstants.CREATED_AT, createdAt.toString());
    EntityUtil.putIfNonEmpty(map, ActivityConstants.ASSET_ID, assetId.toString());
    EntityUtil.putIfNonEmpty(map, ActivityConstants.OPERATION, operation);
    EntityUtil.putIfNonEmpty(map, ActivityConstants.API, api);
    EntityUtil.putIfNonEmpty(map, ActivityConstants.METHOD, method);
    if (size != null && size > 0) map.put(ActivityConstants.SIZE, size);
    EntityUtil.putIfNonEmpty(map, ActivityConstants.ROLE, role);
    EntityUtil.putIfNonEmpty(map, ActivityConstants.USER_ID, userId.toString());
    EntityUtil.putIfNonEmpty(map, ActivityConstants.ORIGIN_SERVER, originServer);
    EntityUtil.putIfNonEmpty(map, ActivityConstants.MYACTIVITY_ENABLED, myactivityEnabled);
    return map;
  }

  @Override
  public JsonObject toJson() {
    JsonObject json = new JsonObject();
    putIfNonEmpty(json, ActivityConstants.ID, id.toString());
    putIfNonEmpty(json, ActivityConstants.ASSET_NAME, assetName);
    putIfNonEmpty(json, ActivityConstants.ASSET_TYPE, assetType);
    putIfNonEmpty(json, ActivityConstants.OPERATION, operation);
    putIfNonEmpty(json, ActivityConstants.CREATED_AT, createdAt);
    putIfNonEmpty(json, ActivityConstants.ASSET_ID, assetId);
    putIfNonEmpty(json, ActivityConstants.API, api);
    putIfNonEmpty(json, ActivityConstants.METHOD, method);
    if (size != null && size > 0) json.put(ActivityConstants.SIZE, size);
    putIfNonEmpty(json, ActivityConstants.ROLE, role);
    putIfNonEmpty(json, ActivityConstants.USER_ID, userId.toString());
    putIfNonEmpty(json, ActivityConstants.ORIGIN_SERVER, originServer);
    return json;
  }

  private <T> void putIfNonEmpty(JsonObject json, String key, T value) {
    if (value != null) {
      json.put(key, value.toString());
    }
  }

  @Override
  public String getTableName() {
    return ACTIVITY_LOG_TABLE_NAME;
  }
}
