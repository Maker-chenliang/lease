package com.maker.lease.web.admin.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.maker.lease.model.entity.LeaseTerm;
import com.maker.lease.web.admin.service.LeaseTermService;
import com.maker.lease.web.admin.mapper.LeaseTermMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;

/**
* @author liubo
* @description 针对表【lease_term(租期)】的数据库操作Service实现
* @createDate 2023-07-24 15:48:00
*/
@Service
public class LeaseTermServiceImpl extends ServiceImpl<LeaseTermMapper, LeaseTerm>
    implements LeaseTermService{
    @Autowired
    private LeaseTermMapper leaseTermMapper;
    @Override
    public List<LeaseTerm> selectListByroomId(Long id) {
        return leaseTermMapper.selectListByRoomId(id);
    }

    @Cacheable(cacheNames = "admin:term", key = "'all'")
    @Override
    public List<LeaseTerm> listLeaseTerm() {
        return this.list();
    }

    @CacheEvict(cacheNames = "admin:term", key = "'all'")
    @Override
    public void saveOrUpdateLeaseTerm(LeaseTerm leaseTerm) {
        this.saveOrUpdate(leaseTerm);
    }

    @CacheEvict(cacheNames = "admin:term", key = "'all'")
    @Override
    public void removeLeaseTermById(Long id) {
        this.removeById(id);
    }
}




