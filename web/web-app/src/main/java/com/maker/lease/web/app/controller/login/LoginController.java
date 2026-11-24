package com.maker.lease.web.app.controller.login;


import com.maker.lease.common.exception.LeaseException;
import com.maker.lease.common.login.LoginUserHolder;
import com.maker.lease.common.result.Result;
import com.maker.lease.common.result.ResultCodeEnum;
import com.maker.lease.model.entity.UserInfo;
import com.maker.lease.web.app.service.LoginService;
import com.maker.lease.web.app.vo.user.LoginVo;
import com.maker.lease.web.app.vo.user.UserInfoVo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

@Tag(name = "登录管理")
@RestController
@RequestMapping("/app/")
public class LoginController {
    @Autowired
    private LoginService loginService;
    @GetMapping("login/getCode")
    @Operation(summary = "获取短信验证码")
    public Result getCode(@RequestParam String phone) {
        loginService.sendCode(phone);
        return Result.ok();
    }

    @PostMapping("login")
    @Operation(summary = "登录")
    public Result<String> login(@RequestBody LoginVo loginVo) {
        String jwt = loginService.login(loginVo);
        return Result.ok(jwt);
    }

    @GetMapping("info")
    @Operation(summary = "获取登录用户信息")
    public Result<UserInfoVo> info() {
        UserInfo user =loginService.getUserInfoById(LoginUserHolder.getLoginUser().getUserId());
        return Result.ok(new UserInfoVo(user.getNickname(), user.getAvatarUrl()));
    }
}

