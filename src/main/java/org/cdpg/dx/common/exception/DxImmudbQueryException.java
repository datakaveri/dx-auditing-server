package org.cdpg.dx.common.exception;

public class DxImmudbQueryException extends DxPgException {
    public DxImmudbQueryException(String message) {
        super(DxErrorCodes.IMMUDB_QUERY_ERROR, message);
    }

    public DxImmudbQueryException(String message, Throwable cause) {
        super(DxErrorCodes.IMMUDB_QUERY_ERROR, message, cause);
    }
}
