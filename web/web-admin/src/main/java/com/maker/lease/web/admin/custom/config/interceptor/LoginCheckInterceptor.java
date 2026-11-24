package com.maker.lease.web.admin.custom.config.interceptor;

import com.maker.lease.common.exception.LeaseException;
import com.maker.lease.common.login.LoginIdentity;
import com.maker.lease.common.login.LoginUser;
import com.maker.lease.common.login.LoginUserHolder;
import com.maker.lease.common.result.ResultCodeEnum;
import com.maker.lease.common.utils.JwtUtil;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.jetbrains.annotations.Nullable;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.HandlerInterceptor;
@Component
public class LoginCheckInterceptor implements HandlerInterceptor {
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String header = request.getHeader("access-token");
        if (!StringUtils.hasText(header)) {
            throw new LeaseException(ResultCodeEnum.ADMIN_LOGIN_AUTH); // 305
        }
        Claims claims = JwtUtil.parseJwt(header);
        Long userId = claims.get("userId", Long.class);
        String userName = claims.get("userName", String.class);
        String userIdentity = claims.get("userIdentity", String.class);

        // 只允许后台账号（超级管理员/管理员/普通员工）访问，拒绝 app 用户 token
        if (!LoginIdentity.isBackend(userIdentity)) {
            throw new LeaseException(ResultCodeEnum.ADMIN_ACCESS_FORBIDDEN); // 309
        }
        LoginUserHolder.setLoginUser(new LoginUser(userId, userName, userIdentity));
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, @Nullable Exception ex) throws Exception {
        LoginUserHolder.clear();
    }
}
