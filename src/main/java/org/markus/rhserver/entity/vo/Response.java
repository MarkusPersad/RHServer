package org.markus.rhserver.entity.vo;
import org.markus.rhserver.enums.ResponseCodeEnum;
import org.markus.rhserver.exception.BusinessException;

import java.io.Serial;
import java.io.Serializable;
public record Response<T>(String status, Integer code,String message, T data) implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
    private static final String SUCCESS_STATUS = "success";
    private static final String FAIL_STATUS = "error";

    /**
     * 创建一个表示操作成功的响应对象
     *
     * @param <T> 泛型类型，表示响应数据的类型
     * @param data 响应中包含的数据
     * @return 包含成功状态、状态码、消息和数据的Response对象
     */
    public  static <T> Response<T>  getSuccessResponse(T data){
        return new Response<>(SUCCESS_STATUS, ResponseCodeEnum.SUCCESS.getCode(),ResponseCodeEnum.SUCCESS.getMessage(),data);
    }

    /**
     * 创建一个表示操作失败的响应对象，通常用于处理业务异常情况
     *
     * @param <T> 泛型类型，表示响应数据的类型
     * @param biz BusinessException业务异常对象，从中获取错误码和错误消息
     * @param data 响应中包含的数据
     * @return 包含失败状态、错误码、错误消息和数据的Response对象
     */
    public static <T> Response<T> getErrorResponse(BusinessException biz,T data){
        return new Response<>(FAIL_STATUS,biz.getCode(),biz.getMessage(),data);
    }

    /**
     * 创建一个表示服务器内部错误的响应对象
     *
     * @param <T> 泛型类型，表示响应数据的类型
     * @param data 响应中包含的数据
     * @return 包含失败状态、内部服务器错误码(500)、错误消息和数据的Response对象
     */
    public static <T> Response<T> getErrorResponse(T data){
        return new Response<>(FAIL_STATUS,ResponseCodeEnum.INTERNAL_SERVER_ERROR.getCode(), ResponseCodeEnum.INTERNAL_SERVER_ERROR.getMessage(),data);
    }

    /**
     * 根据指定的响应码枚举创建一个表示操作失败的响应对象
     *
     * @param <T> 泛型类型，表示响应数据的类型
     * @param codeEnum 响应码枚举，包含错误码和错误消息
     * @param data 响应中包含的数据
     * @return 包含失败状态、指定错误码、错误消息和数据的Response对象
     */
    public static <T> Response<T> getErrorResponse(ResponseCodeEnum codeEnum,T data){
        return new Response<>(FAIL_STATUS,codeEnum.getCode(),codeEnum.getMessage(),data);
    }
}