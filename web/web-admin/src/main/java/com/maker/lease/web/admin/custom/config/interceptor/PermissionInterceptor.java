package com.maker.lease.web.admin.custom.config.interceptor;

import com.maker.lease.common.annotation.RequireIdentity;
import com.maker.lease.common.exception.LeaseException;
import com.maker.lease.common.login.LoginUser;
import com.maker.lease.common.login.LoginUserHolder;
import com.maker.lease.common.result.ResultCodeEnum;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Arrays;
@Component
public class PermissionInterceptor implements HandlerInterceptor {
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if (!(handler instanceof HandlerMethod method)) return true; //不是controller方法放行

        // 方法上的注解优先，其次类上的
        RequireIdentity required = method.getMethodAnnotation(RequireIdentity.class); // 获取方法上的注解
        if (required == null) required = method.getBeanType().getAnnotation(RequireIdentity.class); // 获取类上的注解
        if (required == null) return true;   // 没标注 = 不限制

        LoginUser user = LoginUserHolder.getLoginUser();
        if (user == null) throw new LeaseException(ResultCodeEnum.ADMIN_LOGIN_AUTH);

        boolean allowed = Arrays.stream(required.value())
                .anyMatch(id -> id.equals(user.getUserIdentity()));
        if (!allowed) throw new LeaseException(ResultCodeEnum.ADMIN_ACCESS_FORBIDDEN);
        return true;

    }
}

