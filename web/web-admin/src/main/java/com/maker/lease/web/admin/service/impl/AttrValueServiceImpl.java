package com.maker.lease.web.admin.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.maker.lease.model.entity.AttrValue;
import com.maker.lease.web.admin.service.AttrValueService;
import com.maker.lease.web.admin.mapper.AttrValueMapper;
import com.maker.lease.web.admin.vo.attr.AttrValueVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
* @author liubo
* @description 针对表【attr_value(房间基本属性值表)】的数据库操作Service实现
* @createDate 2023-07-24 15:48:00
*/
@Service
public class AttrValueServiceImpl extends ServiceImpl<AttrValueMapper, AttrValue>
    implements AttrValueService{

    @Autowired
    private AttrValueMapper attrValueMapper;
    @Override
    public List<AttrValueVo> selectListByRoomId(Long id) {
        return attrValueMapper.selectListByRoomId(id);
    }
}




