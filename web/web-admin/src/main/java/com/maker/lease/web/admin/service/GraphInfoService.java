package com.maker.lease.web.admin.service;

import com.maker.lease.model.entity.GraphInfo;
import com.baomidou.mybatisplus.extension.service.IService;
import com.maker.lease.model.enums.ItemType;
import com.maker.lease.web.admin.vo.graph.GraphVo;

import java.util.List;

/**
* @author liubo
* @description 针对表【graph_info(图片信息表)】的数据库操作Service
* @createDate 2023-07-24 15:48:00
*/
public interface GraphInfoService extends IService<GraphInfo> {

    List<GraphVo> selectListByItemTypeAndId(ItemType itemType, Long id);
}
