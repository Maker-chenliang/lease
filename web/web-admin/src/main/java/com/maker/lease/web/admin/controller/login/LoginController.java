package com.maker.lease.web.admin.controller.login;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.maker.lease.common.login.LoginUserHolder;
import com.maker.lease.common.result.Result;
import com.maker.lease.common.utils.JwtUtil;
import com.maker.lease.web.admin.service.LoginService;
import com.maker.lease.web.admin.vo.login.CaptchaVo;
import com.maker.lease.web.admin.vo.login.LoginVo;
import com.maker.lease.web.admin.vo.system.user.SystemUserInfoVo;
import io.jsonwebtoken.Claims;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@Tag(name = "后台管理系统登录管理")
@RestController
@RequestMapping("/admin")
public class LoginController {
    @Autowired
    private LoginService loginService;
    @Operation(summary = "获取图形验证码")
    @GetMapping("login/captcha")
    public Result<CaptchaVo> getCaptcha() {
        CaptchaVo captchaVo= loginService.getCaptcha();
        return Result.ok(captchaVo);
    }

    @Operation(summary = "登录")
    @PostMapping("login")
    public Result<String> login(@RequestBody LoginVo loginVo) {
        String jwt=loginService.login(loginVo);
        return Result.ok(jwt);
    }

    @Operation(summary = "获取登陆用户个人信息")
    @GetMapping("info")
    public Result<SystemUserInfoVo> info() {
        SystemUserInfoVo systemUserInfoVo= loginService.getUserInfoById(LoginUserHolder.getLoginUser().getUserId());
        return Result.ok(systemUserInfoVo);
    }
}