package mingovvv.common.exception;

import lombok.Getter;
import mingovvv.common.constants.ResultCode;

@Getter
public abstract class GlobalException extends RuntimeException {

    private final ResultCode.Error resultCode;

    protected GlobalException(ResultCode.Error resultCode) {
        this.resultCode = resultCode;
    }

    protected GlobalException(ResultCode.Error resultCode, String message) {
        super(message);
        this.resultCode = resultCode;
    }

    protected GlobalException(ResultCode.Error resultCode, String message, Throwable cause) {
        super(message, cause);
        this.resultCode = resultCode;
    }

}
