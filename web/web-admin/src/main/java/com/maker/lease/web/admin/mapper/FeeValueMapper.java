package com.maker.lease.web.admin.mapper;

import com.maker.lease.model.entity.FeeValue;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.maker.lease.web.admin.vo.fee.FeeValueVo;

import java.util.List;

/**
 * @author liubo
 * @description 针对表【fee_value(杂项费用值表)】的数据库操作Mapper
 * @createDate 2023-07-24 15:48:00
 * @Entity com.maker.lease.model.FeeValue
 */
public interface FeeValueMapper extends BaseMapper<FeeValue> {

    List<FeeValueVo> selectListByApartmentId(Long id);
}




