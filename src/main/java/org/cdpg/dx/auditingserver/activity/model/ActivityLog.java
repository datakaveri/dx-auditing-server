package org.cdpg.dx.auditingserver.activity.model;

import static org.cdpg.dx.auditingserver.activity.util.ActivityConstants.ACTIVITY_LOG_TABLE_NAME;

import io.vertx.core.json.JsonObject;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.cdpg.dx.auditingserver.activity.util.ActivityConstants;
import org.cdpg.dx.common.util.DateTimeHelper;
import org.cdpg.dx.database.postgres.base.entity.BaseEntity;
import org.cdpg.dx.database.postgres.util.EntityUtil;

public record ActivityLog(
    Optional<UUID> id,
    String assetName,
    String assetType,
    String operation,
    Optional<LocalDateTime> createdAt,
    Optional<UUID> assetId,
    String api,
    String method,
    Optional<Long> size,
    String role,
    Optional<UUID> userId,
    String originServer,
    boolean myactivityEnabled)
    implements BaseEntity<ActivityLog> {

  public static ActivityLog fromJson(JsonObject json) {
    return new ActivityLog(
        EntityUtil.parseUUID(json.getString(ActivityConstants.ID)),
        json.getString(ActivityConstants.ASSET_NAME, ""),
        json.getString(ActivityConstants.ASSET_TYPE, ""),
        json.getString(ActivityConstants.OPERATION, ""),
        DateTimeHelper.parse(json.getString(ActivityConstants.CREATED_AT, "")),
        EntityUtil.parseUUID(json.getString(ActivityConstants.ASSET_ID)),
        json.getString(ActivityConstants.API, ""),
        json.getString(ActivityConstants.METHOD, ""),
        Optional.ofNullable(json.getLong(ActivityConstants.SIZE)),
        json.getString(ActivityConstants.ROLE, ""),
        EntityUtil.parseUUID(json.getString(ActivityConstants.USER_ID)),
        json.getString(ActivityConstants.ORIGIN_SERVER, ""),
        json.getBoolean(ActivityConstants.MYACTIVITY_ENABLED, false));
  }

  @Override
  public Map<String, Object> toNonEmptyFieldsMap() {
    Map<String, Object> map = new HashMap<>();
    EntityUtil.putIfPresent(map, ActivityConstants.ID, id.map(UUID::toString));
    EntityUtil.putIfNonEmpty(map, ActivityConstants.ASSET_NAME, assetName);
    EntityUtil.putIfNonEmpty(map, ActivityConstants.ASSET_TYPE, assetType);
    EntityUtil.putIfNonEmpty(map, ActivityConstants.OPERATION, operation);
    createdAt.ifPresent(val -> map.put(ActivityConstants.CREATED_AT, val.toString()));
    EntityUtil.putIfPresent(map, ActivityConstants.ASSET_ID, id.map(UUID::toString));
    EntityUtil.putIfNonEmpty(map, ActivityConstants.API, api);
    EntityUtil.putIfNonEmpty(map, ActivityConstants.METHOD, method);
    size.ifPresent(val -> map.put(ActivityConstants.SIZE, val));
    EntityUtil.putIfNonEmpty(map, ActivityConstants.ROLE, role);
    EntityUtil.putIfPresent(map, ActivityConstants.USER_ID, userId.map(UUID::toString));
    EntityUtil.putIfNonEmpty(map, ActivityConstants.ORIGIN_SERVER, originServer);
    map.put(ActivityConstants.MYACTIVITY_ENABLED, myactivityEnabled);
    return map;
  }

  private void putIfNonEmpty(JsonObject json, String key, String value) {
    if (value != null && !value.isEmpty()) {
      json.put(key, value);
    }
  }

  private <T> void putIfPresent(JsonObject json, String key, Optional<T> valueOpt) {
    valueOpt.ifPresent(val -> json.put(key, val.toString()));
  }

  @Override
  public JsonObject toJson() {
    JsonObject json = new JsonObject();
    putIfPresent(json, ActivityConstants.ID, id.map(UUID::toString));
    putIfNonEmpty(json, ActivityConstants.ASSET_NAME, assetName);
    putIfNonEmpty(json, ActivityConstants.ASSET_TYPE, assetType);
    putIfNonEmpty(json, ActivityConstants.OPERATION, operation);
    putIfPresent(json, ActivityConstants.CREATED_AT, createdAt.map(DateTimeHelper::format));
    putIfPresent(json, ActivityConstants.ASSET_ID, assetId);
    putIfNonEmpty(json, ActivityConstants.API, api);
    putIfNonEmpty(json, ActivityConstants.METHOD, method);
    putIfPresent(json, ActivityConstants.SIZE, size);
    putIfNonEmpty(json, ActivityConstants.ROLE, role);
    putIfPresent(json, ActivityConstants.USER_ID, userId);
    putIfNonEmpty(json, ActivityConstants.ORIGIN_SERVER, originServer);
    json.put(ActivityConstants.MYACTIVITY_ENABLED, myactivityEnabled);
    return json;
  }

  @Override
  public String getTableName() {
    return ACTIVITY_LOG_TABLE_NAME;
  }
}
