package iudx.auditing.server.queryStrategy.util;

public class Constants {
    public static final String USER_ID = "userid";
    public static final String USER_ROLE = "userRole";
    public static final String ID = "id";
    public static final String API = "api";
    public static final String HTTP_METHOD = "httpMethod";
    public static final String METHOD = "method";
    public static final String BODY = "body";
    public static final String IUDX_ID = "iudxID";
    public static final String IID = "iid";
    public static final String RESPONSE_SIZE = "response_size";
    public static final String TABLE_NAME = "tableName";
    public static final String WRITE_QUERY4CAT =
            "INSERT INTO $0 (id, userRole, userId, iid, api, method, time, iudxID) VALUES ('$1','$2','$3','$4','$5','$6',$7,'$8')";
    public static final String WRITE_QUERY4RS =
            "INSERT INTO $0 (id,api,userid,epochtime,resourceid,isotime,providerid,size) VALUES ('$1','$2','$3',$4,'$5','$6','$7',$8)";
    public static final String WRITE_QUERY4AUTH =
            "INSERT INTO $0 (id,body,endpoint,method,time,userid) VALUES ('$1','$2','$3','$4',$5,'$6')";


}
