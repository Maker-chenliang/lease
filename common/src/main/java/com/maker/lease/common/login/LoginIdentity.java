package com.maker.lease.common.login;

/**
 * 登录用户身份标识。
 * admin 端取自 system_user.type（ADMIN / COMMON / SUPER_ADMIN），
 * app 端统一为 APP_USER。
 */
public final class LoginIdentity {

    /**
     * 后台超级管理员（type=3），拥有全部权限
     */
    public static final String SUPER_ADMIN = "SUPER_ADMIN";

    /**
     * 后台管理员（type=0）
     */
    public static final String ADMIN = "ADMIN";

    /**
     * 后台普通员工（type=1）
     */
    public static final String COMMON = "COMMON";

    /**
     * app 端用户
     */
    public static final String APP_USER = "APP_USER";

    /**
     * 是否为后台账号（超级管理员/管理员/普通员工）
     */
    public static boolean isBackend(String identity) {
        return SUPER_ADMIN.equals(identity)
                || ADMIN.equals(identity)
                || COMMON.equals(identity);
    }

    /**
     * 是否为超级管理员
     */
    public static boolean isSuperAdmin(String identity) {
        return SUPER_ADMIN.equals(identity);
    }

    /**
     * 是否为管理员
     */
    public static boolean isAdmin(String identity) {
        return ADMIN.equals(identity);
    }

    private LoginIdentity() {
    }
}
