package org.cdpg.dx.auditingserver.activity.service;

import io.vertx.core.Future;
import java.util.UUID;
import org.cdpg.dx.auditingserver.activity.model.ActivityLog;
import org.cdpg.dx.auditingserver.activity.model.ActivityLogRequest;
import org.cdpg.dx.auditingserver.activity.model.Pagination;

public interface ActivityService {
  /**
   * This method is used to get the activity log for a given user.
   *
   * @param userId The ID of the user for whom the activity log is to be fetched.
   * @return A Future containing the activity log for the specified user.
   */
  Future<Pagination<ActivityLog>> getActivityLogByUserId(ActivityLogRequest activityLogRequest);

  /**
   * This method is used to get all activity logs for admin users.
   *
   * @return A Future containing a list of all activity logs.
   */
  Future<Pagination<ActivityLog>> getAllActivityLogsForAdmin(
      ActivityLogRequest activityLogAdminRequest);

  /**
   * This method is used to create a new activity log.
   *
   * @param activityLogEntity The activity log to be created.
   * @return A Future containing the created activity log.
   */
  Future<Void> insertActivityLogIntoDb(ActivityLog activityLogEntity);

}
