package com.maker.lease.common.annotation;


import com.maker.lease.common.login.LoginIdentity;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface RequireIdentity {
    String[] value() default {
            LoginIdentity.SUPER_ADMIN,
            LoginIdentity.ADMIN,
            LoginIdentity.COMMON
    };
}
