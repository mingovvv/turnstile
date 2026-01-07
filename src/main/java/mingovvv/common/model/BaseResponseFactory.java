package mingovvv.common.model;


import mingovvv.common.constants.ResultCode;

public class BaseResponseFactory {

    private final static String DETAIL_MESSAGE_FORMAT = "%s(%s)";

    private static final BaseResponseFactory INSTANCE = new BaseResponseFactory();

    private BaseResponseFactory() {}

    public static <T> BaseResponse<T> create(T t) {
        return INSTANCE.internalCreate(t);
    }

    public static <T> BaseResponse<T> create(ResultCode.Success successEnum) {
        return INSTANCE.internalCreate(successEnum);
    }

    public static <T> BaseResponse<T> create(ResultCode.Success successEnum, T data) {
        return INSTANCE.internalCreate(successEnum, data);
    }

    public static <T> BaseResponse<T> create(ResultCode.Error errorEnum) {
        return INSTANCE.internalCreate(errorEnum);
    }

    public static <T> BaseResponse<T> create(ResultCode.Error errorEnum, T data) {
        return INSTANCE.internalCreate(errorEnum, data);
    }

    public static <T> BaseResponse<T> createDetail(ResultCode.Error errorEnum, String detailMessage) {
        return INSTANCE.internalCreate(errorEnum, DETAIL_MESSAGE_FORMAT.formatted(errorEnum.getMessage(), detailMessage));
    }

    private <T> BaseResponse<T> internalCreate(T data) {
        return new BaseResponse<>(ResultCode.Success.OK.getCode(), ResultCode.Success.OK.getMessage(), data);
    }

    private <T> BaseResponse<T> internalCreate(ResultCode.Success resultCode) {
        return new BaseResponse<>(resultCode.getCode(), resultCode.getMessage());
    }

    private <T> BaseResponse<T> internalCreate(ResultCode.Success resultCode, T data) {
        return new BaseResponse<>(resultCode.getCode(), resultCode.getMessage(), data);
    }

    private <T> BaseResponse<T> internalCreate(ResultCode.Error resultCode) {
        return new BaseResponse<>(resultCode.getCode(), resultCode.getMessage());
    }

    private <T> BaseResponse<T> internalCreate(ResultCode.Error resultCode, T data) {
        return new BaseResponse<>(resultCode.getCode(), resultCode.getMessage(), data);
    }

    private <T> BaseResponse<T> internalCreate(ResultCode.Error resultCode, String detailMessage) {
        return new BaseResponse<>(resultCode.getCode(), detailMessage);
    }

}
