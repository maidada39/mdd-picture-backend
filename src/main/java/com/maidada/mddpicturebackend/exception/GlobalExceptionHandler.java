package com.maidada.mddpicturebackend.exception;


import com.maidada.mddpicturebackend.common.BaseResponse;
import com.maidada.mddpicturebackend.common.ResultUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 全局异常处理器
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    /**
     * 参数校验异常捕获(body参数)
     *
     * @param e e
     * @return {@link BaseResponse }<{@link ? }>
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public BaseResponse<?> handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        BindingResult bindingResult = e.getBindingResult();

        String errorMsg = bindingResult.getFieldErrors().stream().map(DefaultMessageSourceResolvable::getDefaultMessage)
                .collect(Collectors.joining(","));
        log.error(errorMsg, e);
        return ResultUtils.error(ErrorCode.PARAMS_ERROR.getCode(), errorMsg);
    }

    /**
     * 参数校验异常捕获(query参数)
     *
     * @param e e
     * @return {@link BaseResponse }<{@link ? }>
     */
    @ExceptionHandler(BindException.class)
    public BaseResponse<?> bindExceptionHandler(BindException e) {
        List<ObjectError> allErrors = e.getBindingResult().getAllErrors();
        String errorMsg = allErrors
                .stream().map(ObjectError::getDefaultMessage)
                .collect(Collectors.joining(","));
        log.error(errorMsg, e);
        return ResultUtils.error(ErrorCode.PARAMS_ERROR.getCode(), errorMsg);
    }

    @ExceptionHandler(BusinessException.class)
    public BaseResponse<?> businessExceptionHandler(BusinessException e) {
        log.error("BusinessException", e);
        return ResultUtils.error(e.getCode(), e.getMessage());
    }

    @ExceptionHandler(RuntimeException.class)
    public BaseResponse<?> businessExceptionHandler(RuntimeException e) {
        log.error("RuntimeException", e);
        return ResultUtils.error(ErrorCode.SYSTEM_ERROR, "系统错误");
    }
}
