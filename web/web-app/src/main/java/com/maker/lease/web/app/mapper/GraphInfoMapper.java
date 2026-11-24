package com.maker.lease.web.app.mapper;

import com.maker.lease.model.entity.GraphInfo;
import com.maker.lease.model.enums.ItemType;
import com.maker.lease.web.app.vo.graph.GraphVo;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import java.util.List;

/**
* @author liubo
* @description 针对表【graph_info(图片信息表)】的数据库操作Mapper
* @createDate 2023-07-26 11:12:39
* @Entity com.maker.lease.model.entity.GraphInfo
*/
public interface GraphInfoMapper extends BaseMapper<GraphInfo> {

    List<GraphVo> getRoomGraphList(Long id);

    List<GraphVo> getApartmentGraphList(Long id);
}




