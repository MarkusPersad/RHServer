package org.markus.rhserver.controller;

import cn.dev33.satoken.exception.NotLoginException;
import cn.dev33.satoken.exception.NotRoleException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.NonNull;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.babyfish.jimmer.sql.exception.SaveException;
import org.markus.rhserver.entity.vo.Response;
import org.markus.rhserver.enums.ResponseCodeEnum;
import org.markus.rhserver.exception.BusinessException;
import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.NoHandlerFoundException;

import java.lang.reflect.Method;
import java.util.NoSuchElementException;
import java.util.Objects;

@Slf4j
@RestControllerAdvice
class GlobalExceptionHandler implements AsyncUncaughtExceptionHandler {
    /**
     * 统一异常处理方法，处理控制器中未捕获的异常
     * 根据异常类型返回相应的错误响应
     *
     * @param exception 捕获到的异常对象
     * @param request HTTP请求对象
     * @return 包含错误信息的响应对象
     */
    @ExceptionHandler(Exception.class)
    public Object handleException(Exception exception, HttpServletRequest request){
        log.atError().log("请求错误，请求地址：{},错误信息:",request.getRequestURI(),exception);
        return switch (exception) {
            case NoHandlerFoundException _, NoSuchElementException _ ->
                // 处理404未找到异常
                    Response.getErrorResponse(ResponseCodeEnum.NOT_FOUND, null);
            case BusinessException biz ->
                // 处理业务异常
                    Response.getErrorResponse(biz, null);
            case MethodArgumentNotValidException err ->
                // 处理参数验证异常
                    Response.getErrorResponse(ResponseCodeEnum.BAD_REQUEST, Objects.requireNonNull(err.getBindingResult().getFieldError()).getDefaultMessage());
            case BindException _ ->
                // 处理参数绑定异常
                    Response.getErrorResponse(ResponseCodeEnum.BAD_REQUEST, null);
            case SaveException.NotUnique _ ->
                // 处理重复键异常
                    Response.getErrorResponse(ResponseCodeEnum.ALREADY_EXISTS, null);
            case NotRoleException _ ->
                // 处理没有权限异常
                    Response.getErrorResponse(ResponseCodeEnum.FORBIDDEN, null);
            case NotLoginException _ ->
                // 处理未登录异常
                    Response.getErrorResponse(ResponseCodeEnum.UNAUTHORIZED, null);
            case null, default ->
                // 处理其他未预期的服务器内部异常
                    Response.getErrorResponse(ResponseCodeEnum.INTERNAL_SERVER_ERROR, null);
        };
    }

    /**
     * 处理异步任务执行过程中未捕获的异常
     * 当异步方法执行出现异常时，记录错误日志并抛出业务异常
     *
     * @param ex 异常对象
     * @param method 出现异常的方法
     * @param params 方法调用时的参数
     */
    @SneakyThrows
    @Override
    public void handleUncaughtException(@NonNull Throwable ex, Method method, @NonNull Object... params) {
        log.atError().log("异步任务执行异常，方法：{},参数：{},错误信息:", method.getName(),params,ex);
        throw new BusinessException(ResponseCodeEnum.INTERNAL_SERVER_ERROR);
    }

}
