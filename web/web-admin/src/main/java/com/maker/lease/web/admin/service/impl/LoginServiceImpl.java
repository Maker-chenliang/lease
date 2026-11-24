package com.maker.lease.web.admin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.maker.lease.common.constant.RedisConstant;
import com.maker.lease.common.exception.LeaseException;
import com.maker.lease.common.login.LoginIdentity;
import com.maker.lease.common.result.Result;
import com.maker.lease.common.result.ResultCodeEnum;
import com.maker.lease.common.utils.JwtUtil;
import com.maker.lease.model.entity.SystemUser;
import com.maker.lease.model.enums.BaseStatus;
import com.maker.lease.model.enums.SystemUserType;
import com.maker.lease.web.admin.mapper.SystemUserMapper;
import com.maker.lease.web.admin.service.LoginService;
import com.maker.lease.web.admin.vo.login.CaptchaVo;
import com.maker.lease.web.admin.vo.login.LoginVo;
import com.maker.lease.web.admin.vo.system.user.SystemUserInfoVo;
import com.wf.captcha.SpecCaptcha;
import com.wf.captcha.base.Captcha;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Locale;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
public class LoginServiceImpl implements LoginService {

    @Autowired
    private StringRedisTemplate redisTemplate;
    @Autowired
    private SystemUserMapper systemUserMapper;


    // 最大允许失败次数
    private static final int MAX_FAIL_COUNT = 5;
    // 锁定时间（分钟）
    private static final long LOCK_TIME = 3;


    @Override
    public CaptchaVo getCaptcha() {
        SpecCaptcha specCaptcha = new SpecCaptcha(130, 48, 4);// 长、宽、验证码位数
        specCaptcha.setCharType(Captcha.TYPE_DEFAULT);// 字母数字组合

        String code = specCaptcha.text().toLowerCase();//获取验证码的值
        String key = RedisConstant.ADMIN_LOGIN_PREFIX + UUID.randomUUID();
        String image=specCaptcha.toBase64();
        redisTemplate.opsForValue().set(key,code,RedisConstant.ADMIN_LOGIN_CAPTCHA_TTL_SEC, TimeUnit.SECONDS);
        return new CaptchaVo(image,key);
    }

    @Override
    public String login(LoginVo loginVo) {
        String limitKey=RedisConstant.ADMIN_LOGIN_PREFIX+loginVo.getUsername();
        // 检查是否已经被锁定
        String failCountStr=redisTemplate.opsForValue().get(limitKey);
        if(failCountStr!=null&&Integer.parseInt(failCountStr)>=MAX_FAIL_COUNT){
            Long expire=redisTemplate.getExpire(limitKey,TimeUnit.SECONDS);
            throw new LeaseException(ResultCodeEnum.ADMIN_ACCOUNT_ERROR.getCode(),"密码错误次数过多，请"+expire+"秒后重试");
        }

        // 1.判断是否输入了验证码
        if(!StringUtils.hasText(loginVo.getCaptchaCode())){
            throw new  LeaseException(ResultCodeEnum.ADMIN_CAPTCHA_CODE_NOT_FOUND);
        }
        // 2.验证码是否过期
        String code = redisTemplate.opsForValue().get(loginVo.getCaptchaKey());
        if(code==null){
            throw new LeaseException(ResultCodeEnum.ADMIN_CAPTCHA_CODE_EXPIRED);
        }
        // 3.判断验证码是否错误
        if(!code.equals(loginVo.getCaptchaCode().toLowerCase(Locale.ROOT))){
            throw new LeaseException(ResultCodeEnum.ADMIN_CAPTCHA_CODE_ERROR);
        }

        LambdaQueryWrapper<SystemUser> systemUserWrapper = new LambdaQueryWrapper<>();
        // 登录时显式把 password 查出来
        systemUserWrapper
                .select(SystemUser::getId, SystemUser::getUsername,SystemUser::getStatus, SystemUser::getPassword,SystemUser::getType)
                .eq(SystemUser::getUsername, loginVo.getUsername());
        SystemUser systemUser = systemUserMapper.selectOne(systemUserWrapper);
        // 4.校验用户是否存在
        if(systemUser==null){
            throw new LeaseException(ResultCodeEnum.ADMIN_ACCOUNT_NOT_EXIST_ERROR);
        }
        // 5.校验该用户状态是否为启用
        if(systemUser.getStatus()== BaseStatus.DISABLE){
            throw new LeaseException(ResultCodeEnum.ADMIN_ACCOUNT_DISABLED_ERROR);
        }


        // 6.校验密码是否正确
        if(!systemUser.getPassword().equals(DigestUtils.md5Hex(loginVo.getPassword()))){
            // 记录次数，increment 会原子性地把值 +1，并返回自增后的结果
            Long currentFailCount = redisTemplate.opsForValue().increment(limitKey);

            // 计算剩余次数
            int remainingAttempts = MAX_FAIL_COUNT - currentFailCount.intValue();
            // 如果是最后一次失败，设置过期时间
            if (remainingAttempts == 0) {
                redisTemplate.expire(limitKey, LOCK_TIME, TimeUnit.MINUTES);
            }
            if (remainingAttempts > 0) {

                throw new LeaseException(ResultCodeEnum.ADMIN_ACCOUNT_ERROR.getCode(),"密码错误，您还有 " + remainingAttempts + " 次机会");
            } else {
                throw new LeaseException(ResultCodeEnum.ADMIN_ACCOUNT_ERROR.getCode(),"密码错误次数过多，账号已锁定 " + LOCK_TIME + " 分钟");
            }


        }
        // 7.密码正确，清空redisLimit
        redisTemplate.delete(limitKey);

        // 设置身份,system_user.type: 0=管理员，1=普通用户，3=超级管理员
        SystemUserType type = systemUser.getType();
        String userIdentity = type == null ? LoginIdentity.COMMON : switch (type) {
            case ADMIN -> LoginIdentity.ADMIN;              // type = 0
            case COMMON -> LoginIdentity.COMMON;            // type = 1
            case SUPER_ADMIN -> LoginIdentity.SUPER_ADMIN;  // type = 3
        };
        return JwtUtil.generateJwt(systemUser.getId(), systemUser.getUsername(), userIdentity);
    }

    @Override
    public SystemUserInfoVo getUserInfoById(Long userId) {
        SystemUser systemUser = systemUserMapper.selectById(userId);
        SystemUserInfoVo systemUserInfoVo=new SystemUserInfoVo();
        systemUserInfoVo.setName(systemUser.getName());
        systemUserInfoVo.setAvatarUrl(systemUser.getAvatarUrl());
        systemUserInfoVo.setType(systemUser.getType());
        return systemUserInfoVo;
    }
}
