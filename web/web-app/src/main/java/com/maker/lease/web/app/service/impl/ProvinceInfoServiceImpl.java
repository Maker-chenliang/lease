package com.maker.lease.web.app.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.maker.lease.model.entity.ProvinceInfo;
import com.maker.lease.web.app.service.ProvinceInfoService;
import com.maker.lease.web.app.mapper.ProvinceInfoMapper;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;

/**
* @author liubo
* @description 针对表【province_info】的数据库操作Service实现
* @createDate 2023-07-26 11:12:39
*/
@Service
public class ProvinceInfoServiceImpl extends ServiceImpl<ProvinceInfoMapper, ProvinceInfo>
    implements ProvinceInfoService{

    @Override
    @Cacheable(cacheNames = "app:province",key = "'all'")
    public List<ProvinceInfo> listProvince() {
        return this.list();
    }
}




