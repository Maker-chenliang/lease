package com.maker.lease.web.admin.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.maker.lease.model.entity.FacilityInfo;
import com.maker.lease.model.enums.ItemType;
import com.maker.lease.web.admin.service.FacilityInfoService;
import com.maker.lease.web.admin.mapper.FacilityInfoMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;

/**
* @author liubo
* @description 针对表【facility_info(配套信息表)】的数据库操作Service实现
* @createDate 2023-07-24 15:48:00
*/
@Service
public class FacilityInfoServiceImpl extends ServiceImpl<FacilityInfoMapper, FacilityInfo>
    implements FacilityInfoService{
    @Autowired
    private FacilityInfoMapper facilityInfoMapper;
    @Override
    public List<FacilityInfo> selectListByRoomId(Long id) {
        return facilityInfoMapper.selectByRoomId(id);
    }

    @Cacheable(cacheNames = "admin:facility", key = "#type == null ? 'all' : 'type:' + #type.name()")
    @Override
    public List<FacilityInfo> listFacility(ItemType type) {
        return this.list(new LambdaQueryWrapper<FacilityInfo>()
                .eq(type != null, FacilityInfo::getType, type));
    }

    @CacheEvict(cacheNames = "admin:facility", allEntries = true)
    @Override
    public void saveOrUpdateFacility(FacilityInfo facilityInfo) {
        this.saveOrUpdate(facilityInfo);
    }

    @CacheEvict(cacheNames = "admin:facility", allEntries = true)
    @Override
    public void removeFacilityById(Long id) {
        this.removeById(id);
    }
}




