package org.cdpg.dx.auditing.activity.service;

import io.vertx.core.Future;
import org.cdpg.dx.auditing.activity.model.ActivityLog;

public interface ActivityService {
  /**
   * This method is used to get the activity log for a given user.
   *
   * @param userId The ID of the user for whom the activity log is to be fetched.
   * @return A Future containing the activity log for the specified user.
   */
  Future<ActivityLog> getActivityLog(String userId);
}
