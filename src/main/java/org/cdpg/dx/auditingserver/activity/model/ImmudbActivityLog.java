package org.cdpg.dx.auditingserver.activity.model;

import io.vertx.core.json.JsonObject;
import java.util.LinkedHashMap;
import java.util.Map;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cdpg.dx.auditingserver.activity.util.ActivityConstants;
import org.cdpg.dx.database.immudb.util.DbConstants;
import org.cdpg.dx.database.postgres.util.EntityUtil;

/**
 * Represents an activity log entry in the Immudb database. This record is used to store and
 * retrieve activity logs related to assets and operations.
 */
public record ImmudbActivityLog(
    String id,
    String assetName,
    String assetId,
    String assetType,
    String operation,
    String createdAt,
    String api,
    String method,
    String size,
    String role,
    String userId,
    String originServer,
    String myactivityEnabled,
    String shortDescription) {
  private static final Logger LOGGER = LogManager.getLogger(ImmudbActivityLog.class);

  public static ImmudbActivityLog fromJson(JsonObject json) {
    return new ImmudbActivityLog(
        json.getString(DbConstants.DB_ID),
        json.getString(DbConstants.DB_ASSET_NAME),
        json.getString(DbConstants.DB_ASSET_ID),
        json.getString(DbConstants.DB_ASSET_TYPE),
        json.getString(DbConstants.DB_OPERATION),
        json.getString(DbConstants.DB_CREATED_AT),
        json.getString(DbConstants.DB_API),
        json.getString(DbConstants.DB_METHOD),
        json.getString(DbConstants.DB_SIZE, String.valueOf(0L)),
        json.getString(DbConstants.DB_ROLE),
        json.getString(DbConstants.DB_USER_ID),
        json.getString(DbConstants.DB_ORIGIN_SERVER),
        json.getString(DbConstants.DB_MYACTIVITY_ENABLED),
        json.getString(DbConstants.DB_SHORT_DESCRIPTION));
  }

  public Map<String, Object> toNonEmptyFieldsMap() {
    Map<String, Object> map = new LinkedHashMap<>();
    EntityUtil.putIfNonEmpty(map, DbConstants.DB_ID, id);
    EntityUtil.putIfNonEmpty(map, DbConstants.DB_ASSET_NAME, assetName);
    EntityUtil.putIfNonEmpty(map, DbConstants.DB_ASSET_ID, assetId);
    EntityUtil.putIfNonEmpty(map, DbConstants.DB_ASSET_TYPE, assetType);
    EntityUtil.putIfNonEmpty(map, DbConstants.DB_OPERATION, operation);
    EntityUtil.putIfNonEmpty(map, DbConstants.DB_CREATED_AT, createdAt);
    EntityUtil.putIfNonEmpty(map, DbConstants.DB_API, api);
    EntityUtil.putIfNonEmpty(map, DbConstants.DB_METHOD, method);
    EntityUtil.putIfNonEmpty(map, DbConstants.DB_SIZE, size);
    EntityUtil.putIfNonEmpty(map, DbConstants.DB_ROLE, role);
    EntityUtil.putIfNonEmpty(map, DbConstants.DB_USER_ID, userId);
    EntityUtil.putIfNonEmpty(map, DbConstants.DB_ORIGIN_SERVER, originServer);
    EntityUtil.putIfNonEmpty(map, DbConstants.DB_MYACTIVITY_ENABLED, myactivityEnabled);
    EntityUtil.putIfNonEmpty(map, DbConstants.DB_SHORT_DESCRIPTION, shortDescription);
    LOGGER.debug("Converted ImmudbActivityLog to non-empty fields map: {}", map);
    return map;
  }

  public JsonObject toJson() {
    JsonObject json = new JsonObject();
    putIfNonEmpty(json, ActivityConstants.ID, id);
    putIfNonEmpty(json, ActivityConstants.ASSET_NAME, assetName);
    putIfNonEmpty(json, ActivityConstants.ASSET_TYPE, assetType);
    putIfNonEmpty(json, ActivityConstants.OPERATION, operation);
    putIfNonEmpty(json, ActivityConstants.CREATED_AT, createdAt);
    putIfNonEmpty(json, ActivityConstants.ASSET_ID, assetId);
    putIfNonEmpty(json, ActivityConstants.API, api);
    putIfNonEmpty(json, ActivityConstants.METHOD, method);
    putIfNonEmpty(json, ActivityConstants.SIZE, size);
    putIfNonEmpty(json, ActivityConstants.ROLE, role);
    putIfNonEmpty(json, ActivityConstants.USER_ID, userId);
    putIfNonEmpty(json, ActivityConstants.ORIGIN_SERVER, originServer);
    putIfNonEmpty(json, ActivityConstants.SHORT_DESCRIPTION, shortDescription);
    putIfNonEmpty(json, ActivityConstants.MYACTIVITY_ENABLED, myactivityEnabled);
    return json;
  }

  private <T> void putIfNonEmpty(JsonObject json, String key, T value) {
    if (value != null) {
      json.put(key, value);
    }
  }
}
