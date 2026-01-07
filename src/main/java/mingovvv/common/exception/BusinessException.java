package mingovvv.common.exception;

import mingovvv.common.constants.ResultCode;

public class BusinessException extends GlobalException {

    protected BusinessException(ResultCode.Error resultCode) {
        super(resultCode);
    }

    protected BusinessException(ResultCode.Error resultCode, String message) {
        super(resultCode, message);
    }

    protected BusinessException(ResultCode.Error resultCode, String message, Throwable cause) {
        super(resultCode, message, cause);
    }

}
