package org.cdpg.dx.auditingserver.activity.service;

import io.vertx.core.Future;
import java.util.List;
import java.util.UUID;
import org.cdpg.dx.auditingserver.activity.model.ActivityLogEntity;

public interface ActivityService {
  /**
   * This method is used to get the activity log for a given user.
   *
   * @param userId The ID of the user for whom the activity log is to be fetched.
   * @return A Future containing the activity log for the specified user.
   */
  Future<List<ActivityLogEntity>> getActivityLogByUserId(UUID userId);

  /**
   * This method is used to create a new activity log.
   *
   * @param activityLogEntity The activity log to be created.
   * @return A Future containing the created activity log.
   */
  Future<Void> insertActivityLogIntoDb(ActivityLogEntity activityLogEntity);
}
