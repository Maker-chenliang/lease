package com.maker.lease.web.app.service;

import com.maker.lease.model.entity.UserInfo;
import com.maker.lease.web.app.vo.user.LoginVo;

public interface LoginService {
    void sendCode(String phone);

    String login(LoginVo loginVo);

    UserInfo getUserInfoById(Long userId);
}
