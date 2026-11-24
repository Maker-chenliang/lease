package com.maker.lease.web.app.vo.appointment;

import com.maker.lease.model.entity.ViewAppointment;
import com.maker.lease.web.app.vo.apartment.ApartmentItemVo;
import com.maker.lease.web.app.vo.room.RoomItemVo;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;


@Data
@Schema(description = "APP端预约看房详情")
public class AppointmentDetailVo extends ViewAppointment {

    @Schema(description = "公寓基本信息")
    private ApartmentItemVo apartmentItemVo;

    @Schema(description = "预约房间基本信息")
    private RoomItemVo roomItemVo;
}
