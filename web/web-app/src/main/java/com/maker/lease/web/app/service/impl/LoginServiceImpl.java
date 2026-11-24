package com.maker.lease.web.app.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.maker.lease.common.constant.RedisConstant;
import com.maker.lease.common.exception.LeaseException;
import com.maker.lease.common.login.LoginIdentity;
import com.maker.lease.common.result.ResultCodeEnum;
import com.maker.lease.common.utils.JwtUtil;
import com.maker.lease.model.entity.UserInfo;
import com.maker.lease.model.enums.BaseStatus;
import com.maker.lease.web.app.service.LoginService;
import com.maker.lease.web.app.service.SmsService;
import com.maker.lease.web.app.service.UserInfoService;
import com.maker.lease.web.app.vo.user.LoginVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.concurrent.TimeUnit;

@Service
public class LoginServiceImpl implements LoginService {
    @Autowired
    private SmsService smsService;// 阿里云验证码服务
    @Autowired
    private StringRedisTemplate redisTemplate;
    @Autowired
    private UserInfoService userInfoService;
    @Override
    public void sendCode(String phone) {
        String limitKey = "app:login:sms:limit:" + phone;
        // 首次发送验证码时创建10分钟的缓存，并返回true，10分钟没到再次发送时返回false
        Boolean success = redisTemplate.opsForValue().setIfAbsent(limitKey, "1", RedisConstant.APP_LOGIN_CODE_RESEND_TIME_SEC, TimeUnit.SECONDS);
        if (!Boolean.TRUE.equals(success)) {
            throw new LeaseException(ResultCodeEnum.APP_SEND_SMS_TOO_OFTEN);// 验证码发送频繁
        }
        String code;
        try {
             code = smsService.sendCode(phone);
        } catch (RuntimeException e) {
            redisTemplate.delete(limitKey);   // 发送失败放行，允许重试
            throw e;
        }
        redisTemplate.opsForValue().set(RedisConstant.APP_LOGIN_PREFIX+phone,code,RedisConstant.APP_LOGIN_CODE_TTL_SEC, TimeUnit.SECONDS);// 存验证码
    }

    @Override
    public String login(LoginVo loginVo) {
        if(!StringUtils.hasText(loginVo.getPhone()))
        {
            throw new LeaseException(ResultCodeEnum.APP_LOGIN_PHONE_EMPTY);
        }
        if(!StringUtils.hasText(loginVo.getCode())){
            throw new LeaseException(ResultCodeEnum.APP_LOGIN_CODE_EMPTY);
        }
        String key=RedisConstant.APP_LOGIN_PREFIX+loginVo.getPhone();
        String code =redisTemplate.opsForValue().get(key);
        if (!StringUtils.hasText(code)) {
            throw new LeaseException(ResultCodeEnum.APP_LOGIN_CODE_EXPIRED);
        }
        if (!code.equals(loginVo.getCode())) {
            throw new LeaseException(ResultCodeEnum.APP_LOGIN_CODE_ERROR);
        }
        redisTemplate.delete(key);   // 一次性
        LambdaQueryWrapper<UserInfo> userWrapper=new LambdaQueryWrapper<>();
        userWrapper.eq(UserInfo::getPhone,loginVo.getPhone());
        UserInfo userInfo = userInfoService.getOne(userWrapper);
        if(userInfo==null){
            userInfo = new UserInfo();
            userInfo.setPhone(loginVo.getPhone());
            userInfo.setStatus(BaseStatus.ENABLE);
            userInfo.setNickname("用户-"+loginVo.getPhone().substring(6));
            userInfoService.save(userInfo);
        }
        if (userInfo.getStatus().equals(BaseStatus.DISABLE)) {
            throw new LeaseException(ResultCodeEnum.APP_ACCOUNT_DISABLED_ERROR);
        }
        // 统一标定app端用户
        return JwtUtil.generateJwt(userInfo.getId(), userInfo.getPhone(), LoginIdentity.APP_USER);
    }

    @Override
    public UserInfo getUserInfoById(Long userId) {
        UserInfo userInfo = userInfoService.getById(userId);
        return userInfo;
    }
}
