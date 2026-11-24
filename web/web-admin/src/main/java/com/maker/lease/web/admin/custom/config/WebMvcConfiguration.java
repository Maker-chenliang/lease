package com.maker.lease.web.admin.custom.config;

import com.maker.lease.web.admin.custom.config.converter.StringToBaseEnumConverterFactory;
import com.maker.lease.web.admin.custom.config.interceptor.LoginCheckInterceptor;
import com.maker.lease.web.admin.custom.config.interceptor.PermissionInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.format.FormatterRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfiguration implements WebMvcConfigurer {
    @Autowired
    private StringToBaseEnumConverterFactory stringToBaseEnumConverterFactory;
    @Autowired
    private LoginCheckInterceptor loginCheckInterceptor;
    @Autowired
    private PermissionInterceptor permissionInterceptor;

    @Override
    public void addFormatters(FormatterRegistry registry) {
        registry.addConverterFactory(this.stringToBaseEnumConverterFactory);
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(loginCheckInterceptor) //登录校验
                .addPathPatterns("/admin/**").
                excludePathPatterns("/admin/login/**"); // 先：解析 token 放进 ThreadLocal
        registry.addInterceptor(permissionInterceptor)  // 读 ThreadLocal 判断身份，权限校验
                .addPathPatterns("/admin/**")
                .excludePathPatterns("/admin/login/**");
    }
}
