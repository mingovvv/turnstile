package mingovvv.common.exception;

import mingovvv.common.constants.ResultCode;

public class ExternalApiException extends GlobalException {

    public ExternalApiException() {
        super(ResultCode.Error.API_CALL_FAILED);
    }

    // 외부API 에러 메시지를 그대로 로그에 남기고 싶을 때
    public ExternalApiException(String externalMessage) {
        super(ResultCode.Error.API_CALL_FAILED, externalMessage);
    }

    // 외부API 에러 메시지를 그대로 로그에 남기고 싶을 때
    public ExternalApiException(String externalMessage, Throwable cause) {
        super(ResultCode.Error.API_CALL_FAILED, externalMessage, cause);
    }

}
