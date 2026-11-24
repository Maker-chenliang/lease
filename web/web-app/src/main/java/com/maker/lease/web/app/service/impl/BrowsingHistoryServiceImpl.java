package com.maker.lease.web.app.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.maker.lease.common.login.LoginUserHolder;
import com.maker.lease.model.entity.BrowsingHistory;
import com.maker.lease.web.app.mapper.BrowsingHistoryMapper;
import com.maker.lease.web.app.service.BrowsingHistoryService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.maker.lease.web.app.vo.history.HistoryItemVo;
import com.maker.lease.web.app.vo.room.RoomItemVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.Date;

/**
 * @author liubo
 * @description 针对表【browsing_history(浏览历史)】的数据库操作Service实现
 * @createDate 2023-07-26 11:12:39
 */
@Service
public class BrowsingHistoryServiceImpl extends ServiceImpl<BrowsingHistoryMapper, BrowsingHistory>
        implements BrowsingHistoryService {
    @Autowired
    private BrowsingHistoryMapper mapper;

    @Override
    public IPage<HistoryItemVo> pageHistoryItemVoByUserId(IPage<HistoryItemVo> page, Long userId) {
        return mapper.pageHistoryItemVoByUserId(page,userId);
    }
    @Async
    @Override
    public void saveHistory(Long userId, Long id) {
        LambdaQueryWrapper<BrowsingHistory> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(BrowsingHistory::getRoomId,id)
                .eq(BrowsingHistory::getUserId,userId);
        BrowsingHistory browsingHistory = mapper.selectOne(queryWrapper);
        if(browsingHistory!=null){
            browsingHistory.setBrowseTime(new Date());
            mapper.updateById(browsingHistory);
        }else {
            BrowsingHistory newBrowsingHistory = new BrowsingHistory();
            newBrowsingHistory.setUserId(userId);
            newBrowsingHistory.setRoomId(id);
            newBrowsingHistory.setBrowseTime(new Date());
            mapper.insert(newBrowsingHistory);
        }
    }
}