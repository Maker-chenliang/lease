package com.maker.lease.common.exception;

import com.maker.lease.common.result.Result;
import com.maker.lease.common.result.ResultCodeEnum;
import io.minio.errors.MinioException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.CannotGetJdbcConnectionException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

@Slf4j
@ControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 数据库连不上：数据库宕机、网络不通、连接池拿不到连接，更新/插入失败等
     * 对应异常链 CannotGetJdbcConnectionException -> SQLTransientConnectionException / CommunicationsException
     */
    @ExceptionHandler(DataAccessException.class)
    @ResponseBody
    public Result<Void> handleDbConnectionError(DataAccessException e) {
        log.error("数据库异常", e);
        return Result.build(null, ResultCodeEnum.SERVICE_ERROR);
    }
    // 全局异常
    @ExceptionHandler(Exception.class) //需要捕获的异常
    @ResponseBody //响应体
    public Result<Void> error(Exception e) {
        log.error("系统异常", e);
        return Result.build(null, ResultCodeEnum.FAIL);
    }

    @ExceptionHandler(MinioException.class)
    @ResponseBody
    public Result<Void> handUploadError(MinioException e){
        log.error("MinIO异常" ,e);
        return Result.build(null,ResultCodeEnum.FILE_UPLOAD_ERROR);
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    @ResponseBody
    public Result<Void> handleMaxUploadSize(MaxUploadSizeExceededException e) {
        log.warn("上传文件超过大小限制", e);
        return Result.build(null, ResultCodeEnum.FILE_SIZE_EXCEEDED_ERROR);
    }
    // 自定义异常
    @ExceptionHandler(LeaseException.class)
    @ResponseBody
    public Result<Void> handleLeaseException(LeaseException e) {
        e.printStackTrace();
        log.warn("业务异常：{}", e.getMessage(),e);
        return Result.fail(null,e.getCode(),e.getMessage() );
    }
    // 参数异常处理
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseBody
    public Result<Void> handleMethodArgumentNotValidException(MethodArgumentNotValidException e){
        String message = e.getBindingResult().getAllErrors().isEmpty()
                ? ResultCodeEnum.PARAM_ERROR.getMessage()
                : e.getBindingResult().getAllErrors().get(0).getDefaultMessage();
        return Result.fail(null, ResultCodeEnum.PARAM_ERROR.getCode(), message);
    }

}
