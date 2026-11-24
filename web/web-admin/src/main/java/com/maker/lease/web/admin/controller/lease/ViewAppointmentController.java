package com.maker.lease.web.admin.controller.lease;


import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.maker.lease.common.result.Result;
import com.maker.lease.model.entity.BaseEntity;
import com.maker.lease.model.entity.ViewAppointment;
import com.maker.lease.model.enums.AppointmentStatus;
import com.maker.lease.web.admin.service.ViewAppointmentService;
import com.maker.lease.web.admin.vo.appointment.AppointmentQueryVo;
import com.maker.lease.web.admin.vo.appointment.AppointmentVo;
import com.baomidou.mybatisplus.core.metadata.IPage;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.xml.crypto.Data;
import java.util.Date;


@Tag(name = "预约看房管理")
@RequestMapping("/admin/appointment")
@RestController
public class ViewAppointmentController {
    @Autowired
    private ViewAppointmentService service;
    @Operation(summary = "分页查询预约信息")
    @GetMapping("page")
    public Result<IPage<AppointmentVo>> page(@RequestParam long current, @RequestParam long size, AppointmentQueryVo queryVo) {
        IPage<AppointmentVo> page = new Page<>(current, size);
        IPage<AppointmentVo> result= service.pageAppointmentByQuery(page,queryVo);
        return Result.ok(result);
    }

    @Operation(summary = "根据id更新预约状态")
    @PostMapping("updateStatusById")
    public Result updateStatusById(@RequestParam Long id, @RequestParam AppointmentStatus status) {
        LambdaUpdateWrapper<ViewAppointment> updateWrapper=new LambdaUpdateWrapper<>();
        updateWrapper.eq(ViewAppointment::getId,id);
        updateWrapper.set(ViewAppointment::getAppointmentStatus,status);
        updateWrapper.set(BaseEntity::getUpdateTime, new Date());
        service.update(updateWrapper);
        return Result.ok();
    }

}
