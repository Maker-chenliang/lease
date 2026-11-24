package com.maker.lease.web.admin.vo.system.user;

import com.maker.lease.model.enums.SystemUserType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;

@Schema(description = "员工基本信息")
@Data
public class SystemUserInfoVo {

    @Schema(description = "用户姓名")
    private String name;

    @Schema(description = "用户头像")
    private String avatarUrl;

    @Schema(description = "用户类型：0 管理员 1 普通用户 3 超级管理员")
    private SystemUserType type;
}
