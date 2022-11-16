package iudx.auditing.server.queryStrategy.util;

public class Constants {
    public static final String SIZE = "size";
    public static final String ID = "id";
    public static final String USER_ID = "userid";
    public static final String API = "api";
    public static final String TABLE_NAME = "tableName";
    public static final String WRITE_QUERY =
            "INSERT INTO $0 (id,api,userid,epochtime,resourceid,isotime,providerid,size) VALUES ('$1','$2','$3',$4,'$5','$6','$7',$8)";
    public static final String DELETE_QUERY =
            "DELETE FROM $0 WHERE id = '$1'";
}
