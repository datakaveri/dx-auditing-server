package org.cdpg.dx.common.exception;

public class DxImmudbQueryException extends BaseDxException {
    public DxImmudbQueryException(String message) {
        super(DxErrorCodes.IMMUDB_QUERY_ERROR, message);
    }

    public DxImmudbQueryException(String message, Throwable cause) {
        super(DxErrorCodes.IMMUDB_QUERY_ERROR, message, cause);
    }
}
