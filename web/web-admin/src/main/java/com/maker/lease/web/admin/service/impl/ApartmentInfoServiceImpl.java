package com.maker.lease.web.admin.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.maker.lease.model.entity.ApartmentInfo;
import com.maker.lease.web.admin.mapper.ApartmentInfoMapper;
import com.maker.lease.web.admin.service.ApartmentInfoService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.maker.lease.web.admin.vo.apartment.ApartmentItemVo;
import com.maker.lease.web.admin.vo.apartment.ApartmentQueryVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author liubo
 * @description 针对表【apartment_info(公寓信息表)】的数据库操作Service实现
 * @createDate 2023-07-24 15:48:00
 */
@Service
public class ApartmentInfoServiceImpl extends ServiceImpl<ApartmentInfoMapper, ApartmentInfo>
        implements ApartmentInfoService {
    @Autowired
    private ApartmentInfoMapper mapper;

    @Override
    public IPage<ApartmentItemVo> pageItem(IPage<ApartmentItemVo> iPage, ApartmentQueryVo queryVo) {
        return mapper.pageItem(iPage,queryVo);
    }
}




