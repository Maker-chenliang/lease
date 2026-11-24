package com.maker.lease.web.admin.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.maker.lease.model.entity.PaymentType;
import com.maker.lease.web.admin.mapper.PaymentTypeMapper;
import com.maker.lease.web.admin.service.PaymentTypeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author liubo
 * @description 针对表【payment_type(支付方式表)】的数据库操作Service实现
 * @createDate 2023-07-24 15:48:00
 */
@Service
public class PaymentTypeServiceImpl extends ServiceImpl<PaymentTypeMapper, PaymentType>
        implements PaymentTypeService {
    @Autowired
    private PaymentTypeMapper paymentTypeMapper;

    @Override
    public List<PaymentType> selectListByRoomId(Long id) {
        return paymentTypeMapper.selectListByRoomId(id);
    }

    @Cacheable(cacheNames = "admin:paymentType", key = "'all'")
    @Override
    public List<PaymentType> listPaymentType() {
        return this.list();
    }

    @Override
    @CacheEvict(cacheNames = "admin:paymentType", key = "'all'")
    public void saveOrUpdatePaymentType(PaymentType paymentType) {
        this.saveOrUpdate(paymentType);
    }

    @CacheEvict(cacheNames = "admin:paymentType", key = "'all'")
    @Override
    public void removePaymentById(Long id) {
        this.removeById(id);
    }
}




