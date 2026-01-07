package mingovvv.common.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import mingovvv.common.constants.ResultCode;

@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class BaseResponse<T> {

    private final boolean status;
    private final String code;
    private final String message;
    private T data;

    public BaseResponse(String code, String message) {
        this.status = ResultCode.isSuccess(code);
        this.code = code;
        this.message = message;
    }

    public BaseResponse(String code, String message, T data) {
        this.status = ResultCode.isSuccess(code);
        this.code = code;
        this.message = message;
        this.data = data;
    }

}
