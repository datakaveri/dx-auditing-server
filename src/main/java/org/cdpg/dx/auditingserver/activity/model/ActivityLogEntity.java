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

public record ActivityLogEntity(
    Optional<UUID> id,
    Optional<UUID> UserId,
    String name,
    Optional<String> description,
    Optional<LocalDateTime> createdAt,
    Optional<LocalDateTime> updatedAt)
    implements BaseEntity<ActivityLogEntity> {

  public static ActivityLogEntity fromJson(JsonObject json) {
    return new ActivityLogEntity(
        EntityUtil.parseUUID(json.getString(ActivityConstants.ID)),
        EntityUtil.parseUUID(json.getString(ActivityConstants.USER_ID)),
        json.getString(ActivityConstants.NAME),
        Optional.ofNullable(json.getString(ActivityConstants.DESCRIPTION)),
        DateTimeHelper.parse(json.getString(ActivityConstants.CREATED_AT)),
        DateTimeHelper.parse(json.getString(ActivityConstants.UPDATED_AT)));
  }

  @Override
  public Map<String, Object> toNonEmptyFieldsMap() {
    Map<String, Object> map = new HashMap<>();
    EntityUtil.putIfPresent(map, ActivityConstants.ID, id.map(UUID::toString));
    EntityUtil.putIfPresent(map, ActivityConstants.USER_ID, UserId.map(UUID::toString));
    EntityUtil.putIfNonEmpty(map, ActivityConstants.NAME, name);
    description.ifPresent(desc -> map.put(ActivityConstants.DESCRIPTION, desc));
    createdAt.ifPresent(val -> map.put(ActivityConstants.CREATED_AT, val.toString()));
    updatedAt.ifPresent(val -> map.put(ActivityConstants.UPDATED_AT, val.toString()));
    return map;
  }

  @Override
  public JsonObject toJson() {
    return new JsonObject()
        .put(ActivityConstants.ID, id.map(UUID::toString).orElse(null))
        .put(ActivityConstants.USER_ID, UserId.map(UUID::toString).orElse(null))
        .put(ActivityConstants.NAME, name)
        .put(ActivityConstants.DESCRIPTION, description.orElse(null))
        .put(ActivityConstants.CREATED_AT, createdAt.map(DateTimeHelper::format).orElse(null))
        .put(ActivityConstants.UPDATED_AT, updatedAt.map(DateTimeHelper::format).orElse(null));
  }

  @Override
  public String getTableName() {
    return ACTIVITY_LOG_TABLE_NAME;
  }
}
