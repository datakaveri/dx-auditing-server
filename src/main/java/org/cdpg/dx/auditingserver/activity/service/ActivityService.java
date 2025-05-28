package org.cdpg.dx.auditingserver.activity.service;

import io.vertx.core.Future;
import java.util.UUID;
import org.cdpg.dx.auditingserver.activity.model.ActivityLog;
import org.cdpg.dx.auditingserver.activity.model.ActivityLogAdminRequest;
import org.cdpg.dx.auditingserver.activity.model.Pagination;

public interface ActivityService {
  /**
   * This method is used to get the activity log for a given user.
   *
   * @param userId The ID of the user for whom the activity log is to be fetched.
   * @return A Future containing the activity log for the specified user.
   */
  Future<Pagination<ActivityLog>> getActivityLogByUserId(UUID userId, int limit, int offset);

  /**
   * This method is used to get all activity logs for admin users.
   *
   * @return A Future containing a list of all activity logs.
   */
  Future<Pagination<ActivityLog>> getAllActivityLogsForAdmin(
      ActivityLogAdminRequest activityLogAdminRequest);

  /**
   * This method is used to create a new activity log.
   *
   * @param activityLogEntity The activity log to be created.
   * @return A Future containing the created activity log.
   */
  Future<Void> insertActivityLogIntoDb(ActivityLog activityLogEntity);

  public Future<Pagination<ActivityLog>> getAllWitPagination(int limit, int offset);
}
