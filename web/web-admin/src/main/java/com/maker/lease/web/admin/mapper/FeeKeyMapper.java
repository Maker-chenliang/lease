package com.maker.lease.web.admin.mapper;

import com.maker.lease.model.entity.FeeKey;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.maker.lease.web.admin.vo.fee.FeeKeyVo;

import java.util.List;

/**
 * @author liubo
 * @description 针对表【fee_key(杂项费用名称表)】的数据库操作Mapper
 * @createDate 2023-07-24 15:48:00
 * @Entity com.maker.lease.model.FeeKey
 */
public interface FeeKeyMapper extends BaseMapper<FeeKey> {

    List<FeeKeyVo> listInfo();
}




