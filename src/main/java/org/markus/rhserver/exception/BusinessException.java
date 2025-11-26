package org.markus.rhserver.exception;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.markus.rhserver.enums.ResponseCodeEnum;

@EqualsAndHashCode(callSuper = false)
@Getter
public class BusinessException extends Exception {
    private ResponseCodeEnum codeEnum;
    private Integer code;
    private  String message;
    public BusinessException(String message,Throwable e) {
        super(message,e);
        this.message = message;
    }
    public BusinessException(String message) {
        this.message = message;
        super(message);
    }
    public BusinessException(Throwable e){
        super(e);
    }
    public BusinessException(ResponseCodeEnum codeEnum){
        this.codeEnum = codeEnum;
        this.code = codeEnum.getCode();
        this.message = codeEnum.getMessage();
        super(codeEnum.getMessage());
    }
    public BusinessException(Integer code,String message){
        this.code = code;
        this.message = message;
        super(message);
    }
    /**
     * 覆盖父类的fillInStackTrace方法，用于填充异常堆栈跟踪信息
     * 此方法被同步以确保线程安全
     *
     * @return 返回当前异常实例本身，不生成堆栈跟踪信息
     */
    @Override
    public synchronized Throwable fillInStackTrace() {
        return this;
    }
}