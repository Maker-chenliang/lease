package com.maker.lease.web.admin.service;

import com.maker.lease.model.entity.PaymentType;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
* @author liubo
* @description 针对表【payment_type(支付方式表)】的数据库操作Service
* @createDate 2023-07-24 15:48:00
*/
public interface PaymentTypeService extends IService<PaymentType> {

    List<PaymentType> selectListByRoomId(Long id);

    List<PaymentType> listPaymentType();

    void saveOrUpdatePaymentType(PaymentType paymentType);

    void removePaymentById(Long id);
}
