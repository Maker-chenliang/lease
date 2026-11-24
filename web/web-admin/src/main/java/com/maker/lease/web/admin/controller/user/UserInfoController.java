package com.maker.lease.web.admin.controller.user;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.maker.lease.common.result.Result;
import com.maker.lease.model.entity.BaseEntity;
import com.maker.lease.model.entity.UserInfo;
import com.maker.lease.model.enums.BaseStatus;
import com.maker.lease.web.admin.service.UserInfoService;
import com.maker.lease.web.admin.vo.user.UserInfoQueryVo;
import com.baomidou.mybatisplus.core.metadata.IPage;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@Tag(name = "用户信息管理")
@RestController
@RequestMapping("/admin/user")
public class UserInfoController {
    @Autowired
    private UserInfoService service;
    @Operation(summary = "分页查询用户信息")
    @GetMapping("page")
    public Result<IPage<UserInfo>> pageUserInfo(@RequestParam long current, @RequestParam long size, UserInfoQueryVo queryVo) {
        IPage<UserInfo> page=new Page<>(current,size);
        LambdaQueryWrapper<UserInfo> userQueryWrapper = new LambdaQueryWrapper<>();
        userQueryWrapper.like(queryVo.getPhone()!=null, UserInfo::getPhone,queryVo.getPhone());
        userQueryWrapper.eq(queryVo.getStatus()!=null, UserInfo::getStatus,queryVo.getStatus());
        IPage<UserInfo> result = service.page(page, userQueryWrapper);
        return Result.ok(result);
    }

    @Operation(summary = "根据用户id更新账号状态")
    @PostMapping("updateStatusById")
    public Result updateStatusById(@RequestParam Long id, @RequestParam BaseStatus status) {
        LambdaUpdateWrapper<UserInfo> userUpdateWrapper = new LambdaUpdateWrapper<>();
        userUpdateWrapper.eq(BaseEntity::getId,id)
                .set(UserInfo::getStatus,status);
        service.update(userUpdateWrapper);
        return Result.ok();
    }
}
