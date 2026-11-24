package com.maker.lease.web.app.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.maker.lease.model.entity.BrowsingHistory;
import com.baomidou.mybatisplus.extension.service.IService;
import com.maker.lease.web.app.vo.history.HistoryItemVo;
import com.maker.lease.web.app.vo.room.RoomItemVo;

/**
* @author liubo
* @description 针对表【browsing_history(浏览历史)】的数据库操作Service
* @createDate 2023-07-26 11:12:39
*/
public interface BrowsingHistoryService extends IService<BrowsingHistory> {

    IPage<HistoryItemVo> pageHistoryItemVoByUserId(IPage<HistoryItemVo> page, Long userId);

    void saveHistory(Long userId, Long id);
}
