package org.cdpg.dx.auditingserver.activity.immudbActivity;

import io.vertx.core.Future;
import org.cdpg.dx.auditingserver.activity.model.ImmudbActivityLog;

public interface ImmudbActivityService {
  /**
   * This method is used to insert an activity log into the immudb database.
   *
   * @param activityLogEntity The activity log to be inserted into immudb.
   * @return A Future indicating the success or failure of the operation.
   */
  Future<Void> insertActivityLogIntoImmudb(ImmudbActivityLog activityLogEntity);

}
