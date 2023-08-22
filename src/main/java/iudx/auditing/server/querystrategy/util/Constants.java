package iudx.auditing.server.querystrategy.util;

public class Constants {
  public static final String SIZE = "response_size";
  public static final String ID = "id";
  public static final String USER_ID = "userid";
  public static final String PRIMARY_KEY = "primaryKey";
  public static final String PROVIDER_ID = "providerID";
  public static final String ISO_TIME = "isoTime";
  public static final String BODY = "body";
  public static final String IUDX_ID = "iudxID";
  public static final String IID = "iid";
  public static final String USER_ROLE = "userRole";
  public static final String HTTP_METHOD = "httpMethod";

  public static final String EPOCH_TIME = "epochTime";
  public static final String API = "api";
  public static final String ITEM_TYPE = "itemType";
  public static final String OGC_PG_TABLE_NAME = "postgresOgcTableName";
  public static final String RS_PG_TABLE_NAME = "postgresRsTableName";
  public static final String RS_SUBS_TABLE_NAME = "postgresSubscriptionAuditTableName";
  public static final String RS_IMMUDB_TABLE_NAME = "immudbRsTableName";
  public static final String OGC_IMMUDB_TABLE_NAME = "immudbOgcTableName";
  public static final String AUTH_PG_TABLE_NAME = "postgresAuthTableName";
  public static final String AUTH_IMMUDB_TABLE_NAME = "immudbAuthTableName";
  public static final String CAT_PG_TABLE_NAME = "postgresCatTableName";
  public static final String CAT_IMMUDB_TABLE_NAME = "immudbCatTableName";
  public static final String RS_WRITE_QUERY_IMMUDB =
      "INSERT INTO $0 (id,api,userid,epochtime,resourceid,isotime,providerid,size) "
          + "VALUES ('$1','$2','$3',$4,'$5','$6','$7',$8)";
  public static final String RS_WRITE_QUERY_PG =
      "INSERT INTO $0 (id,api,userid,epochtime,resourceid,isotime,providerid,size,time)"
          + " VALUES ('$1','$2','$3',$4,'$5','$6','$7',$8,'$9')";
  public static final String CAT_WRITE_QUERY_PG =
      "INSERT INTO $0 (id, userRole, userID, iid, api, method, time, iudxID) "
          + "VALUES ('$1','$2','$3','$4','$5','$6',$7,'$8')";
  public static final String CAT_WRITE_QUERY_IMMUDB =
      "INSERT INTO $0 (id, userRole, userID, iid, api, method, time, iudxID) "
          + "VALUES ('$1','$2','$3','$4','$5','$6',$7,'$8')";
  public static final String AUTH_WRITE_QUERY_PG =
      "INSERT INTO $0 (id,body,endpoint,method,time,userid) VALUES ('$1','$2','$3','$4','$5','$6')";
  public static final String AUTH_WRITE_QUERY_IMMUDB =
      "INSERT INTO $0 (id,body,endpoint,method,time,userid) VALUES ('$1','$2','$3','$4','$5','$6')";
  public static final String DELETE_QUERY = "DELETE FROM $0 WHERE id = '$1';";
  public static final String DELETE_SUBSCRIPTION_QUERY =
      "DELETE FROM $0 WHERE subscription_id = '$1';";

  public static final String RS_SUBS_WRITE_QUERY_PG =
      "INSERT INTO $0 (subscription_id, user_id, event_type, subscription_type, resource_id) VALUES ('$1','$2','$3','$4','$5');";
  public static final String RS_SUBS_UPDATE_QUERY_PG =
      "UPDATE $0 SET event_type = '$1' WHERE subscription_id = '$2'";

  public static final String OGC_WRITE_QUERY_IMMUDB =
          "INSERT INTO $0 (id,api,userid,epochtime,resourceid,isotime,providerid,size,itemType) "
                  + "VALUES ('$1','$2','$3',$4,'$5','$6','$7',$8,'$9')";
  public static final String OGC_WRITE_QUERY_PG =
          "INSERT INTO $0 (id,api,userid,epochtime,resourceid,providerid,itemType,size,time)"
                  + " VALUES ('$1','$2','$3',$4,'$5','$6','$7',$8,'$9')";
}
