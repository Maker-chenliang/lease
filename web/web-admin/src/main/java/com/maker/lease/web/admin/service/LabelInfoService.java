package com.maker.lease.web.admin.service;

import com.maker.lease.model.entity.LabelInfo;
import com.maker.lease.model.enums.ItemType;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
* @author liubo
* @description 针对表【label_info(标签信息表)】的数据库操作Service
* @createDate 2023-07-24 15:48:00
*/
public interface LabelInfoService extends IService<LabelInfo> {

    List<LabelInfo> selectListByRoomId(Long id);

    List<LabelInfo> listLabel(ItemType type);

    void saveOrUpdateLabel(LabelInfo labelInfo);

    void removeLabelById(Long id);
}
