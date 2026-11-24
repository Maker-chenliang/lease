package com.maker.lease.web.admin.controller.system;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.maker.lease.common.annotation.RequireIdentity;
import com.maker.lease.common.exception.LeaseException;
import com.maker.lease.common.login.LoginIdentity;
import com.maker.lease.common.login.LoginUser;
import com.maker.lease.common.login.LoginUserHolder;
import com.maker.lease.common.result.Result;
import com.maker.lease.common.result.ResultCodeEnum;
import com.maker.lease.model.entity.SystemUser;
import com.maker.lease.model.enums.BaseStatus;
import com.maker.lease.model.enums.SystemUserType;
import com.maker.lease.web.admin.service.SystemUserService;
import com.maker.lease.web.admin.vo.system.user.SystemUserItemVo;
import com.maker.lease.web.admin.vo.system.user.SystemUserQueryVo;
import com.baomidou.mybatisplus.core.metadata.IPage;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;


@Tag(name = "后台用户信息管理")
@RequireIdentity({LoginIdentity.SUPER_ADMIN,LoginIdentity.ADMIN}) // 拦截普通用户
@RestController
@RequestMapping("/admin/system/user")
public class SystemUserController {
    @Autowired
    private SystemUserService service;
    @Operation(summary = "根据条件分页查询后台用户列表")
    @GetMapping("page")
    public Result<IPage<SystemUserItemVo>> page(@RequestParam long current, @RequestParam long size, SystemUserQueryVo queryVo) {
        IPage<SystemUserItemVo> page = new Page<>(current, size);
        boolean excludeAdmin=!LoginIdentity.isSuperAdmin(LoginUserHolder.getLoginUser().getUserIdentity());
        IPage<SystemUserItemVo> result= service.pageSystemUserByQuery(page,queryVo,excludeAdmin);
        return Result.ok(result);
    }

    @Operation(summary = "根据ID查询后台用户信息")
    @GetMapping("getById")
    public Result<SystemUserItemVo> getById(@RequestParam Long id) {
        service.checkTarget(id);
        SystemUserItemVo result = service.getSystemUserById(id);
        return Result.ok(result);
    }

    @Operation(summary = "获取个人信息")
    // 方法注解优先于类注解，放开普通用户访问
    @RequireIdentity({LoginIdentity.SUPER_ADMIN, LoginIdentity.ADMIN, LoginIdentity.COMMON})
    @GetMapping("myInfo")
    public Result<SystemUserItemVo> myInfo() {
        // id 只从 token 取
        Long userId = LoginUserHolder.getLoginUser().getUserId();
        return Result.ok(service.getSystemUserById(userId));
    }

    @Operation(summary = "修改个人信息")
    @RequireIdentity({LoginIdentity.SUPER_ADMIN, LoginIdentity.ADMIN, LoginIdentity.COMMON})
    @PostMapping("updateMyInfo")
    public Result updateMyInfo(@Valid @RequestBody SystemUser systemUser) {
        Long userId = LoginUserHolder.getLoginUser().getUserId();

        // id 以 token 为准，只改自己
        systemUser.setId(userId);
        // 用户类型、岗位、账号状态不允许本人修改：置 null 后 MyBatis-Plus 默认忽略这些字段
        systemUser.setType(null);
        systemUser.setPostId(null);
        systemUser.setStatus(null);

        // 检查用户名不重复
        if (systemUser.getUsername() != null) {
            long count = service.count(new LambdaQueryWrapper<SystemUser>()
                    .eq(SystemUser::getUsername, systemUser.getUsername())
                    .ne(SystemUser::getId, userId));
            if (count > 0) {
                throw new LeaseException(ResultCodeEnum.ADMIN_ACCOUNT_EXIST_ERROR);
            }
        }


        if (StringUtils.hasText(systemUser.getPassword())) {
            systemUser.setPassword(DigestUtils.md5Hex(systemUser.getPassword()));
        } else {
            systemUser.setPassword(null);
        }


        service.updateById(systemUser);
        return Result.ok();
    }


    @Operation(summary = "保存或更新后台用户信息")
    @PostMapping("saveOrUpdate")
    public Result saveOrUpdate(@Valid @RequestBody SystemUser systemUser) {
        // 只有超管能把别的用户设置成超管,还要检查操作目标是不是管理员/超管
        LoginUser current = LoginUserHolder.getLoginUser();
        if (!LoginIdentity.isSuperAdmin(current.getUserIdentity())
                && systemUser.getType() == SystemUserType.SUPER_ADMIN) {
            throw new LeaseException(ResultCodeEnum.ADMIN_ACCESS_FORBIDDEN);
        }
        service.checkTarget(systemUser.getId());
        if (systemUser.getId() == null) {
            long count = service.count(new LambdaQueryWrapper<SystemUser>()
                    .eq(SystemUser::getUsername, systemUser.getUsername()));
            if (count > 0) throw new LeaseException(ResultCodeEnum.ADMIN_ACCOUNT_EXIST_ERROR);
        }
        if(systemUser.getPassword() != null){
            systemUser.setPassword(DigestUtils.md5Hex(systemUser.getPassword()));
        }
        service.saveOrUpdate(systemUser);
        return Result.ok();
    }
    @RequireIdentity({LoginIdentity.SUPER_ADMIN, LoginIdentity.ADMIN, LoginIdentity.COMMON})
    @Operation(summary = "判断后台用户名是否可用")
    @GetMapping("isUserNameAvailable")
    public Result<Boolean> isUsernameExists(@RequestParam String username) {
        LambdaQueryWrapper<SystemUser> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SystemUser::getUsername, username);
        long count = service.count(queryWrapper);
        return Result.ok(count == 0);
    }

    @DeleteMapping("deleteById")
    @Operation(summary = "根据ID删除后台用户信息")
    public Result removeById(@RequestParam Long id) {
        service.checkTarget(id);
        service.removeById(id);
        return Result.ok();
    }

    @Operation(summary = "根据ID修改后台用户状态")
    @PostMapping("updateStatusByUserId")
    public Result updateStatusByUserId(@RequestParam Long id, @RequestParam BaseStatus status) {
        service.checkTarget(id);
        LambdaUpdateWrapper<SystemUser> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(SystemUser::getId, id);
        updateWrapper.set(SystemUser::getStatus, status);
        service.update(updateWrapper);
        return Result.ok();
    }
}
