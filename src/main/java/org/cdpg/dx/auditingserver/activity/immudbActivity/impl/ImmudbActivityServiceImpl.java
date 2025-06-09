package org.cdpg.dx.auditingserver.activity.immudbActivity.impl;

import static org.cdpg.dx.database.immudb.util.DbConstants.DB_TABLE_NAME;

import io.vertx.core.Future;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cdpg.dx.auditingserver.activity.immudbActivity.ImmudbActivityService;
import org.cdpg.dx.auditingserver.activity.model.ImmudbActivityLog;
import org.cdpg.dx.database.immudb.query.InsertQuery;
import org.cdpg.dx.database.immudb.service.ImmudbService;

public class ImmudbActivityServiceImpl implements ImmudbActivityService {
  private static final Logger LOGGER = LogManager.getLogger(ImmudbActivityServiceImpl.class);
  private final ImmudbService immudbService;

  public ImmudbActivityServiceImpl(ImmudbService immudbService) {
    this.immudbService = immudbService;
  }

  @Override
  public Future<Void> insertActivityLogIntoImmudb(ImmudbActivityLog activityLogEntity) {
    var columns = activityLogEntity.toNonEmptyFieldsMap().keySet().stream().toList();
    var values = activityLogEntity.toNonEmptyFieldsMap().values().stream().toList();
    LOGGER.debug("Inserting activity log into immudb with columns: {}, values: {}", columns, values);
    InsertQuery insertQuery = new InsertQuery().setTable(DB_TABLE_NAME).setColumns(columns).setValues(values);
    return immudbService.executeQuery(insertQuery).mapEmpty();
  }
}
