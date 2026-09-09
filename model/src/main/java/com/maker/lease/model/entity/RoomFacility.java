package com.maker.lease.model.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

@Schema(description = "房间&配套关联表")
@TableName(value = "room_facility")
@Data
@Builder
public class RoomFacility extends BaseEntity {

    private static final long serialVersionUID = 1L;

    @Schema(description = "房间id")
    @TableField(value = "room_id")
    private Long roomId;

    @Schema(description = "房间设施id")
    @TableField(value = "facility_id")
    private Long facilityId;

}