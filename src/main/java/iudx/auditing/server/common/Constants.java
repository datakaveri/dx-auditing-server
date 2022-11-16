package iudx.auditing.server.common;

public class Constants {
  public static final String RMQ_SERVICE_ADDRESS = "iudx.auditing.rabbit.service";
  public static final String AUDIT_LATEST_QUEUE= "auditing-messages";
  public static final String MSG_PROCESS_ADDRESS = "iudx.auditing.msg.service";
  public static final String IMMUDB_SERVICE_ADDRESS = "iudx.auditing.immudb.service";
  public static final String PG_SERVICE_ADDRESS = "iudx.auditing.postgres.service";
  public static final String INSERT_QUERY_KEY = "insertquery";
  public static final String DELETE_QUERY_KEY = "deletequery";

}
