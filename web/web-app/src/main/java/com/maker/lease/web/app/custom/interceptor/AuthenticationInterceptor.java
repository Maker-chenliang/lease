package com.maker.lease.web.app.custom.interceptor;

import com.maker.lease.common.exception.LeaseException;
import com.maker.lease.common.login.LoginIdentity;
import com.maker.lease.common.login.LoginUser;
import com.maker.lease.common.login.LoginUserHolder;
import com.maker.lease.common.result.ResultCodeEnum;
import com.maker.lease.common.utils.JwtUtil;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class AuthenticationInterceptor implements HandlerInterceptor {
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String token = request.getHeader("access-token");
        if (!StringUtils.hasText(token)) {
            throw new LeaseException(ResultCodeEnum.APP_LOGIN_AUTH);   // 501
        }
        Claims claims = JwtUtil.parseJwt(token);
        Long userId = claims.get("userId", Long.class);
        String username = claims.get("userName", String.class);
        String userIdentity = claims.get("userIdentity", String.class);

        // 只允许app端用户访问，拒绝后台账号token
        if (!LoginIdentity.APP_USER.equals(userIdentity)) {
            throw new LeaseException(ResultCodeEnum.APP_ACCESS_FORBIDDEN); // 508
        }
        LoginUserHolder.setLoginUser(new LoginUser(userId, username, userIdentity));

        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        LoginUserHolder.clear();
    }
}
