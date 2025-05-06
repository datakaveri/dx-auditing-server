package org.cdpg.dx.auditing.activity.model;



import io.vertx.core.json.JsonObject;
import org.cdpg.dx.database.postgres.base.enitty.BaseEntity;
import org.cdpg.dx.auditing.activity.util.Constants;
import org.cdpg.dx.common.util.DateTimeHelper;
import org.cdpg.dx.common.util.EntityUtil;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public record ActivityLog(Optional<UUID> id, String name, Optional<String> description,
                       Optional<LocalDateTime> createdAt, Optional<LocalDateTime> updatedAt)
        implements BaseEntity<ActivityLog> {

    public static final String TABLE_NAME = Constants.TABLE_NAME;

    @Override
    public Map<String, Object> toNonEmptyFieldsMap() {
        Map<String, Object> map = new HashMap<>();
        EntityUtil.putIfPresent(map, Constants.ID, id.map(UUID::toString));
        EntityUtil.putIfNonEmpty(map, Constants.NAME, name);
        description.ifPresent(desc -> map.put(Constants.DESCRIPTION, desc));
        createdAt.ifPresent(val -> map.put(Constants.CREATED_AT, DateTimeHelper.format(val)));
        updatedAt.ifPresent(val -> map.put(Constants.UPDATED_AT, DateTimeHelper.format(val)));
        return map;
    }

    @Override
    public JsonObject toJson() {
        return new JsonObject()
                .put(Constants.ID, id.map(UUID::toString).orElse(null))
                .put(Constants.NAME, name)
                .put(Constants.DESCRIPTION, description.orElse(null))
                .put(Constants.CREATED_AT, createdAt.map(DateTimeHelper::format).orElse(null))
                .put(Constants.UPDATED_AT, updatedAt.map(DateTimeHelper::format).orElse(null));
    }

    public static ActivityLog fromJson(JsonObject json) {
        return new ActivityLog(
                EntityUtil.parseUUID(json.getString(Constants.ID)),
                json.getString(Constants.NAME),
                Optional.ofNullable(json.getString(Constants.DESCRIPTION)),
                DateTimeHelper.parse(json.getString(Constants.CREATED_AT)),
                DateTimeHelper.parse(json.getString(Constants.UPDATED_AT))
        );
    }

    @Override
    public String getTableName() {
        return TABLE_NAME;
    }
}
