package com.maker.lease.web.admin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.maker.lease.model.entity.CityInfo;
import com.maker.lease.web.admin.service.CityInfoService;
import com.maker.lease.web.admin.mapper.CityInfoMapper;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;

/**
* @author liubo
* @description 针对表【city_info】的数据库操作Service实现
* @createDate 2023-07-24 15:48:00
*/
@Service
public class CityInfoServiceImpl extends ServiceImpl<CityInfoMapper, CityInfo>
    implements CityInfoService{

    @Override
    @Cacheable(cacheNames = "admin:city",key = "#provinceId")
    public List<CityInfo> listCity(Long provinceId) {
        return this.list(new LambdaQueryWrapper<CityInfo>().eq(CityInfo::getProvinceId,provinceId));
    }
}




