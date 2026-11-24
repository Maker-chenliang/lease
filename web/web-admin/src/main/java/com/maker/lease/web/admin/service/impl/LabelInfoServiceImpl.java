package com.maker.lease.web.admin.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.maker.lease.model.entity.LabelInfo;
import com.maker.lease.model.enums.ItemType;
import com.maker.lease.web.admin.service.LabelInfoService;
import com.maker.lease.web.admin.mapper.LabelInfoMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;

/**
* @author liubo
* @description 针对表【label_info(标签信息表)】的数据库操作Service实现
* @createDate 2023-07-24 15:48:00
*/
@Service
public class LabelInfoServiceImpl extends ServiceImpl<LabelInfoMapper, LabelInfo>
    implements LabelInfoService{
    @Autowired
    private LabelInfoMapper labelInfoMapper;
    @Override
    public List<LabelInfo> selectListByRoomId(Long id) {
        return labelInfoMapper.selectListByRoomId(id);

    }

    @Cacheable(cacheNames = "admin:label", key = "#type == null ? 'all' : 'type:' + #type.name()")
    @Override
    public List<LabelInfo> listLabel(ItemType type) {
        return this.list(new LambdaQueryWrapper<LabelInfo>()
                .eq(type != null, LabelInfo::getType, type));
    }

    @CacheEvict(cacheNames = "admin:label", allEntries = true)
    @Override
    public void saveOrUpdateLabel(LabelInfo labelInfo) {
        this.saveOrUpdate(labelInfo);
    }

    @CacheEvict(cacheNames = "admin:label", allEntries = true)
    @Override
    public void removeLabelById(Long id) {
        this.removeById(id);
    }
}




