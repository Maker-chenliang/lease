package com.maker.lease.web.admin.custom.aspect;

import com.alibaba.fastjson.JSON;
import com.maker.lease.common.login.LoginUser;
import com.maker.lease.common.login.LoginUserHolder;
import com.maker.lease.common.result.Result;
import com.maker.lease.model.entity.OperateLog;
import com.maker.lease.web.admin.service.OperateLogService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Slf4j
@Aspect
@Component
public class OperateLogAspect {
    @Autowired
    private HttpServletRequest request;

    @Autowired
    private OperateLogService operateLogService;

    /**
     * 切 admin 下所有 Controller 方法。
     * 文件上传接口排除，因为 MultipartFile 没法序列化成 JSON。
     */
    @Around("execution(public * com.maker.lease.web.admin.controller..*.*(..)) " +
            "&& !execution(* com.maker.lease.web.admin.controller.apartment.FileUploadController.*(..))")
    public Object record(ProceedingJoinPoint joinPoint) throws Throwable {

        // GET 查询不记录
        if ("GET".equalsIgnoreCase(request.getMethod())) {
            return joinPoint.proceed();
        }

        long begin = System.currentTimeMillis();
        OperateLog operateLog = new OperateLog();

        // 拦截器已经解析过 JWT，这里直接取；必须在主线程取，异步线程里是 null
        LoginUser loginUser = LoginUserHolder.getLoginUser();
        if (loginUser != null) {
            operateLog.setUserId(loginUser.getUserId());
            operateLog.setUsername(loginUser.getUsername());
        }

        operateLog.setOperateTime(LocalDateTime.now());
        operateLog.setClassName(joinPoint.getTarget().getClass().getName());
        operateLog.setMethodName(joinPoint.getSignature().getName());
        operateLog.setMethodParams(toJson(joinPoint.getArgs()));

        try {
            // 执行原来的 Controller 方法
            Object result = joinPoint.proceed();

            operateLog.setResultCode(200);
            if (result instanceof Result<?> r && r.getCode() != null) {
                operateLog.setResultCode(r.getCode());
            }

            // 登录接口返回的是 JWT，不落库
            if (!request.getRequestURI().contains("/login")) {
                operateLog.setReturnValue(toJson(result));
            }
            return result;

        } catch (Throwable e) {
            // 业务失败也要记录
            operateLog.setResultCode(500);
            operateLog.setErrorMsg(e.getMessage());
            throw e;   // 继续抛给 GlobalExceptionHandler

        } finally {
            operateLog.setCostTime(System.currentTimeMillis() - begin);
            try {
                // 丢进线程池，不阻塞当前请求
                operateLogService.saveAsync(operateLog);
            } catch (Exception e) {
                log.error("提交操作日志失败", e);
            }
        }
    }

    /**
     * 对象转 JSON，并对密码、token 打码。
     */
    private String toJson(Object obj) {
        try {
            String json = JSON.toJSONString(obj);
            return json.replaceAll(
                    "(\"(?i:password|token)\"\\s*:\\s*)\"[^\"]*\"", "$1\"***\"");
        } catch (Exception e) {
            return "序列化失败";
        }
    }
}
