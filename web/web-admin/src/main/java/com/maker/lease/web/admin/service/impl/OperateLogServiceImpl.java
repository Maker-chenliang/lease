package com.maker.lease.web.admin.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.maker.lease.model.entity.OperateLog;
import com.maker.lease.web.admin.mapper.OperateLogMapper;
import com.maker.lease.web.admin.service.OperateLogService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

/**
* @author lengchenliang
* @description 针对表【operate_log(操作日志)】的数据库操作Service实现
* @createDate 2026-10-30 15:24:13
*/
@Slf4j
@Service
public class OperateLogServiceImpl extends ServiceImpl<OperateLogMapper, OperateLog>
    implements OperateLogService {
    @Autowired
    private OperateLogMapper operateLogMapper;
    @Async
    @Override
    public void saveAsync(OperateLog operateLog) {
        try {
            operateLogMapper.insert(operateLog);
        } catch (Exception e) {
            log.error("操作日志写入失败", e);
        }
    }
}




