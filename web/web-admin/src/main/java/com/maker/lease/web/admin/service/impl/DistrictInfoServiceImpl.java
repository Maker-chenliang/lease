package com.maker.lease.web.admin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.maker.lease.model.entity.DistrictInfo;
import com.maker.lease.web.admin.service.DistrictInfoService;
import com.maker.lease.web.admin.mapper.DistrictInfoMapper;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;

/**
* @author liubo
* @description 针对表【district_info】的数据库操作Service实现
* @createDate 2023-07-24 15:48:00
*/
@Service
public class DistrictInfoServiceImpl extends ServiceImpl<DistrictInfoMapper, DistrictInfo>
    implements DistrictInfoService{

    @Override
    @Cacheable(cacheNames = "app:district",key = "#id")
    public List<DistrictInfo> listDistrictInfoByCityId(Long id) {
        return this.list(new LambdaQueryWrapper<DistrictInfo>().eq(DistrictInfo::getCityId,id));
    }
}




