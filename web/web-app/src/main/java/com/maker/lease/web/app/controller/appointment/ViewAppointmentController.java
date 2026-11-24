package com.maker.lease.web.app.controller.appointment;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.maker.lease.common.login.LoginUserHolder;
import com.maker.lease.common.result.Result;
import com.maker.lease.common.result.ResultCodeEnum;
import com.maker.lease.model.entity.LeaseAgreement;
import com.maker.lease.model.entity.RoomInfo;
import com.maker.lease.model.entity.ViewAppointment;
import com.maker.lease.model.enums.AppointmentStatus;
import com.maker.lease.model.enums.LeaseStatus;
import com.maker.lease.model.enums.ReleaseStatus;
import com.maker.lease.web.app.service.LeaseAgreementService;
import com.maker.lease.web.app.service.RoomInfoService;
import com.maker.lease.web.app.service.ViewAppointmentService;
import com.maker.lease.web.app.vo.appointment.AppointmentDetailVo;
import com.maker.lease.web.app.vo.appointment.AppointmentItemVo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Objects;

@Tag(name = "看房预约信息")
@RestController
@RequestMapping("/app/appointment")
public class ViewAppointmentController {
    @Autowired
    private ViewAppointmentService service;
    @Autowired
    private RoomInfoService roomInfoService;
    @Autowired
    private LeaseAgreementService leaseAgreementService;
    @Operation(summary = "保存或更新看房预约")
    @PostMapping("/saveOrUpdate")
    public Result saveOrUpdate(@RequestBody ViewAppointment viewAppointment) {
        // 加个并发防重复提交

        Long userId = LoginUserHolder.getLoginUser().getUserId();
        if (viewAppointment.getId() == null) {
            viewAppointment.setAppointmentStatus(AppointmentStatus.WAITING);
        }
        // 现在只有房间详情页能预约，roomId 必填
        if (viewAppointment.getRoomId() == null) {
            return Result.build(null, ResultCodeEnum.PARAM_ERROR);
        }

        // 更新时校验归属（是否为本人），以数据库记录为准
        if (viewAppointment.getId() != null) {
            ViewAppointment old = service.getById(viewAppointment.getId());
            if (old == null || !Objects.equals(old.getUserId(), userId)) {
                return Result.build(null, ResultCodeEnum.APP_ACCESS_FORBIDDEN);
            }
        }

        // 房间 id 以数据库房间记录为准
        RoomInfo room = roomInfoService.getById(viewAppointment.getRoomId());
        if (room == null) {
            return Result.build(null, ResultCodeEnum.DATA_ERROR);
        }
        // 未发布的房间不能预约
        if (room.getIsRelease() != ReleaseStatus.RELEASED) {
            return Result.build(null, ResultCodeEnum.APP_ROOM_NOT_AVAILABLE);
        }
        // 已有生效中的租约（签约待确认/已签约/退租待确认/续约待确认）不能预约
        Long activeLeaseCount = leaseAgreementService.count(
                new LambdaQueryWrapper<LeaseAgreement>()
                        .eq(LeaseAgreement::getRoomId, viewAppointment.getRoomId())
                        .in(LeaseAgreement::getStatus,
                                LeaseStatus.SIGNING, LeaseStatus.SIGNED,
                                LeaseStatus.WITHDRAWING, LeaseStatus.RENEWING));
        if (activeLeaseCount > 0) {
            return Result.build(null, ResultCodeEnum.APP_ROOM_NOT_AVAILABLE);
        }
        viewAppointment.setApartmentId(room.getApartmentId());

        // 判重：同一用户 + 同一房间 + 待看房
        LambdaQueryWrapper<ViewAppointment> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ViewAppointment::getUserId, userId)
                .eq(ViewAppointment::getRoomId, viewAppointment.getRoomId())
                .eq(ViewAppointment::getAppointmentStatus, AppointmentStatus.WAITING);

        // 重新预约 = 更新自己那条，排除自己
        if (viewAppointment.getId() != null) {
            wrapper.ne(ViewAppointment::getId, viewAppointment.getId());
        }

        if (service.count(wrapper) > 0) {
            return Result.build(null, ResultCodeEnum.REPEAT_SUBMIT);
        }

        viewAppointment.setUserId(userId);
        service.saveOrUpdate(viewAppointment);
        return Result.ok();
    }

    @Operation(summary = "查询个人预约看房列表")
    @GetMapping("listItem")
    public Result<List<AppointmentItemVo>> listItem() {
        List<AppointmentItemVo> appointmentItemVoList=service.getAppointmentItemVo(LoginUserHolder.getLoginUser().getUserId());
        return Result.ok(appointmentItemVoList);
    }

    @GetMapping("getDetailById")
    @Operation(summary = "根据ID查询预约详情信息")
    public Result<AppointmentDetailVo> getDetailById(Long id) {
        ViewAppointment viewAppointment = service.getById(id);
        if (viewAppointment == null
                || !Objects.equals(viewAppointment.getUserId(),
                LoginUserHolder.getLoginUser().getUserId())) {
            return Result.build(null, ResultCodeEnum.APP_ACCESS_FORBIDDEN);
        }
        AppointmentDetailVo appointmentDetailVo= service.getAppointmentDetailById(id);
        return Result.ok(appointmentDetailVo);
    }
    // 查询是否预约了该房间,是就传预约id回去
    @Operation(summary = "查询当前用户对指定房间的待看房预约id")
    @GetMapping("getWaitingIdByRoomId")
    public Result<Long> getWaitingIdByRoomId(@RequestParam Long roomId) {
        Long userId = LoginUserHolder.getLoginUser().getUserId();

        LambdaQueryWrapper<ViewAppointment> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ViewAppointment::getUserId, userId)
                .eq(ViewAppointment::getRoomId, roomId)
                .eq(ViewAppointment::getAppointmentStatus, AppointmentStatus.WAITING)
                .orderByDesc(ViewAppointment::getId)
                .last("limit 1");

        ViewAppointment waiting = service.getOne(wrapper, false);
        return Result.ok(waiting == null ? null : waiting.getId());
    }
    @Operation(summary = "取消预约")
    @PostMapping("cancel")
    public Result cancel(@RequestParam Long id) {
        Long userId = LoginUserHolder.getLoginUser().getUserId();
        ViewAppointment old = service.getById(id);
        // 只能取消自己的预约
        if (old == null || !Objects.equals(old.getUserId(), userId)) {
            return Result.build(null, ResultCodeEnum.APP_ACCESS_FORBIDDEN);
        }
        // 只有待看房状态能取消
        if (old.getAppointmentStatus() != AppointmentStatus.WAITING) {
            return Result.build(null, ResultCodeEnum.DATA_ERROR);
        }
        ViewAppointment update = new ViewAppointment();
        update.setId(id);
        update.setAppointmentStatus(AppointmentStatus.CANCELED);
        service.updateById(update);
        return Result.ok();
    }
}

