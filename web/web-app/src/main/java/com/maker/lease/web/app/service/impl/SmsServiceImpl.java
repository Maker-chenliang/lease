package com.maker.lease.web.app.service.impl;

import com.aliyun.sdk.service.dypnsapi20170525.AsyncClient;
import com.aliyun.sdk.service.dypnsapi20170525.models.CheckSmsVerifyCodeRequest;
import com.aliyun.sdk.service.dypnsapi20170525.models.CheckSmsVerifyCodeResponse;
import com.aliyun.sdk.service.dypnsapi20170525.models.SendSmsVerifyCodeRequest;
import com.aliyun.sdk.service.dypnsapi20170525.models.SendSmsVerifyCodeResponse;
import com.maker.lease.common.sms.AliyunSMSProperties;
import com.maker.lease.web.app.service.SmsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class SmsServiceImpl implements SmsService {

    @Autowired
    private AsyncClient client;

    @Autowired
    private AliyunSMSProperties properties;

    @Override
    public String sendCode(String phone) {
        SendSmsVerifyCodeRequest request = SendSmsVerifyCodeRequest.builder()
                .signName(properties.getSignName())
                .templateCode(properties.getTemplateCode())
                .phoneNumber(phone)
                // ##code## 交给阿里云生成，不要再传具体验证码
                .templateParam("{\"code\":\"##code##\",\"min\":\"10\"}")
                .codeLength(6L)      // 6 位
                .validTime(600L)     // 10 分钟有效
                .returnVerifyCode(true)
                .build();

        SendSmsVerifyCodeResponse response;
        try {
            response = client.sendSmsVerifyCode(request).get();
        } catch (Exception e) {
            log.error("调用阿里云短信接口异常: phone={}", phone, e);
            throw new RuntimeException("短信发送失败", e);
        }

        if (!"OK".equals(response.getBody().getCode())) {
            log.error("短信发送失败: phone={}, code={}, message={}",
                    phone, response.getBody().getCode(), response.getBody().getMessage());
            throw new RuntimeException("短信发送失败：" + response.getBody().getMessage());
        }
        return response.getBody().getModel().getVerifyCode();
    }

    @Override
    public boolean checkCode(String phone, String code) {
        CheckSmsVerifyCodeRequest request = CheckSmsVerifyCodeRequest.builder()
                .phoneNumber(phone)
                .verifyCode(code)
                .build();

        try {
            CheckSmsVerifyCodeResponse response = client.checkSmsVerifyCode(request).get();
            if (!"OK".equals(response.getBody().getCode())) {
                log.error("验证码核验请求失败: phone={}, code={}, message={}",
                        phone, response.getBody().getCode(), response.getBody().getMessage());
                return false;
            }
            // 注意：接口成功不代表验证通过，必须看 VerifyResult
            return "PASS".equals(response.getBody().getModel().getVerifyResult());
        } catch (Exception e) {
            log.error("调用阿里云验证码核验接口异常: phone={}", phone, e);
            return false;
        }
    }
}
