package com.maker.lease.web.admin.service;

import com.maker.lease.model.entity.OperateLog;
import com.baomidou.mybatisplus.extension.service.IService;

/**
* @author lengchenliang
* @description 针对表【operate_log(操作日志)】的数据库操作Service
* @createDate 2026-10-30 15:24:13
*/
public interface OperateLogService extends IService<OperateLog> {


    void saveAsync(OperateLog operateLog);
}
