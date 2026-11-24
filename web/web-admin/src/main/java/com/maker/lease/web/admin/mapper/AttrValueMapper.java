package com.maker.lease.web.admin.mapper;

import com.maker.lease.model.entity.AttrValue;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.maker.lease.web.admin.vo.attr.AttrValueVo;

import java.util.List;

/**
 * @author liubo
 * @description 针对表【attr_value(房间基本属性值表)】的数据库操作Mapper
 * @createDate 2023-07-24 15:48:00
 * @Entity com.maker.lease.model.AttrValue
 */
public interface AttrValueMapper extends BaseMapper<AttrValue> {

    List<AttrValueVo> selectListByRoomId(Long id);
}




