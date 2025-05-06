package org.cdpg.dx.auditing.activity.dao.impl;

import io.vertx.core.json.JsonObject;
import org.cdpg.dx.auditing.activity.dao.ActivityLogDAO;
import org.cdpg.dx.auditing.activity.model.ActivityLog;
import org.cdpg.dx.database.postgres.base.dao.AbstractBaseDAO;
import org.cdpg.dx.database.postgres.service.PostgresService;
import org.cdpg.dx.auditing.activity.util.Constants;

public class ActivityLogDAOImpl extends AbstractBaseDAO<ActivityLog> implements ActivityLogDAO {

    public ActivityLogDAOImpl(PostgresService postgresService) {
        super(postgresService, ActivityLog.TABLE_NAME, Constants.ID, ActivityLog::fromJson);
    }


}
