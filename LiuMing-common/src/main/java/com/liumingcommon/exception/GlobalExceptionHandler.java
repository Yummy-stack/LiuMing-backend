package com.liumingcommon.exception;

import com.liumingcommon.BaseResponse;
import com.liumingcommon.utils.ResultUtils;
import com.liumingmodel.enums.error.ErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public BaseResponse<?> businessExceptionHandler(BusinessException e) {
        log.error(e.toString());
        return ResultUtils.error(e.getCode(), e.getMessage());
    }

    @ExceptionHandler(RuntimeException.class)
    public BaseResponse<?> runtimeExceptionHandler(RuntimeException e) {
        log.error(e.toString());
        return ResultUtils.error(ErrorCode.SYSTEM_ERROR, e.getMessage());
    }
}
