package com.maker.lease.web.app.mapper;

import com.maker.lease.model.entity.BrowsingHistory;
import com.maker.lease.web.app.vo.history.HistoryItemVo;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;

/**
* @author liubo
* @description 针对表【browsing_history(浏览历史)】的数据库操作Mapper
* @createDate 2023-07-26 11:12:39
* @Entity com.maker.lease.model.entity.BrowsingHistory
*/
public interface BrowsingHistoryMapper extends BaseMapper<BrowsingHistory> {


    IPage<HistoryItemVo> pageHistoryItemVoByUserId(IPage<HistoryItemVo> page, Long userId);
}




