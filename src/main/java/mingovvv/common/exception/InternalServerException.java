package mingovvv.common.exception;

import mingovvv.common.constants.ResultCode;

public class InternalServerException extends GlobalException {

    protected InternalServerException() {
        super(ResultCode.Error.SYS_INTERNAL_ERROR);
    }

    protected InternalServerException(String message) {
        super(ResultCode.Error.SYS_INTERNAL_ERROR, message);
    }

    protected InternalServerException(String message, Throwable cause) {
        super(ResultCode.Error.SYS_INTERNAL_ERROR, message, cause);
    }

}
