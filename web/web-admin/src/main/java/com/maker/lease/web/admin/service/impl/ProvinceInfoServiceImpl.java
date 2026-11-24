package com.maker.lease.web.admin.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.maker.lease.model.entity.ProvinceInfo;
import com.maker.lease.web.admin.service.ProvinceInfoService;
import com.maker.lease.web.admin.mapper.ProvinceInfoMapper;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;

/**
* @author liubo
* @description 针对表【province_info】的数据库操作Service实现
* @createDate 2023-07-24 15:48:00
*/
@Service
public class ProvinceInfoServiceImpl extends ServiceImpl<ProvinceInfoMapper, ProvinceInfo>
    implements ProvinceInfoService{

    @Override
    @Cacheable(cacheNames = "admin:province", key = "'all'")
    public List<ProvinceInfo> listProvince() {

        return this.list();
    }
}




