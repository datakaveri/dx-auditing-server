package org.cdpg.dx.common.exception;

import org.cdpg.dx.auth.authentication.exception.AuthenticationException;

public class DxForbiddenException extends BaseDxException {
    public DxForbiddenException(String message) {
        super(DxErrorCodes.FORBIDDEN, message);
    }

    public DxForbiddenException(String message, Throwable cause) {
        super(DxErrorCodes.FORBIDDEN, message, cause);
    }
}