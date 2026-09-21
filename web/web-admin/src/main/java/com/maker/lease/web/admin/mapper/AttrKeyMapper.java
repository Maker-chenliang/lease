package com.maker.lease.web.admin.mapper;

import com.maker.lease.model.entity.AttrKey;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.maker.lease.web.admin.vo.attr.AttrKeyVo;

import java.util.List;

/**
 * @author liubo
 * @description 针对表【attr_key(房间基本属性表)】的数据库操作Mapper
 * @createDate 2023-07-24 15:48:00
 * @Entity com.maker.lease.model.AttrKey
 */
public interface AttrKeyMapper extends BaseMapper<AttrKey> {
    List<AttrKeyVo> listAttrInfo();
}




