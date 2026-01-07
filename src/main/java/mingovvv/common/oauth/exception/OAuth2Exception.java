package mingovvv.common.oauth.exception;

import mingovvv.common.constants.ResultCode;
import mingovvv.common.exception.BusinessException;

/**
 * OAuth 2.0 관련 예외
 * <p>
 * OAuth 2.0 표준 에러를 처리합니다.
 * BusinessException을 상속받아 동일한 예외 처리 프레임워크를 사용합니다.
 */
public class OAuth2Exception extends BusinessException {

    /**
     * ResultCode.Error를 사용하는 생성자
     *
     * @param resultCode 에러 코드
     */
    public OAuth2Exception(ResultCode.Error resultCode) {
        super(resultCode);
    }

    /**
     * ResultCode.Error와 커스텀 메시지를 사용하는 생성자
     *
     * @param resultCode 에러 코드
     * @param message    커스텀 메시지
     */
    public OAuth2Exception(ResultCode.Error resultCode, String message) {
        super(resultCode, message);
    }

}
