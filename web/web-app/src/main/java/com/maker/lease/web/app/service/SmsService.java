package com.maker.lease.web.app.service;

public interface SmsService {
    String sendCode(String phone);
    boolean checkCode(String phone, String code);
}
