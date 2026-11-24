package com.maker.lease.common.login;
// 线程，存储用户信息
public class LoginUserHolder {
      public static ThreadLocal<LoginUser> threadLocal = new ThreadLocal<>();
  
      public static void setLoginUser(LoginUser loginUser) {
          threadLocal.set(loginUser);
      }
  
      public static LoginUser getLoginUser() {
          return threadLocal.get();
      }
  
      public static void clear() {
          threadLocal.remove();
      }
  }