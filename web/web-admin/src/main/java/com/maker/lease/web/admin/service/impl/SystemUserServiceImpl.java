package com.maker.lease.web.admin.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.maker.lease.common.exception.LeaseException;
import com.maker.lease.common.login.LoginIdentity;
import com.maker.lease.common.login.LoginUser;
import com.maker.lease.common.login.LoginUserHolder;
import com.maker.lease.common.result.ResultCodeEnum;
import com.maker.lease.model.entity.SystemPost;
import com.maker.lease.model.entity.SystemUser;
import com.maker.lease.model.enums.SystemUserType;
import com.maker.lease.web.admin.mapper.SystemPostMapper;
import com.maker.lease.web.admin.mapper.SystemUserMapper;
import com.maker.lease.web.admin.service.SystemUserService;
import com.maker.lease.web.admin.vo.system.user.SystemUserItemVo;
import com.maker.lease.web.admin.vo.system.user.SystemUserQueryVo;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Objects;

/**
 * @author liubo
 * @description 针对表【system_user(员工信息表)】的数据库操作Service实现
 * @createDate 2023-07-24 15:48:00
 */
@Service
public class SystemUserServiceImpl extends ServiceImpl<SystemUserMapper, SystemUser>
        implements SystemUserService {
    @Autowired
    private SystemUserMapper systemUserMapper;
    @Autowired
    private SystemPostMapper systemPostMapper;


    @Override
    public IPage<SystemUserItemVo> pageSystemUserByQuery(IPage<SystemUserItemVo> page, SystemUserQueryVo queryVo, Boolean excludeAdmin) {
        return systemUserMapper.pageSystemUserByQuery(page, queryVo, excludeAdmin);
    }

    @Override
    public SystemUserItemVo getSystemUserById(Long id) {

        SystemUser systemUser = systemUserMapper.selectById(id);
        SystemPost systemPost = systemPostMapper.selectById(systemUser.getPostId());
        SystemUserItemVo systemUserItemVo = new SystemUserItemVo();
        BeanUtils.copyProperties(systemUser, systemUserItemVo);
        systemUserItemVo.setPostName(systemPost.getName());
        return systemUserItemVo;
    }

    /**
     * 非超管不能操作“其他人”的管理员/超管账号
     */
    @Override
    public void checkTarget(Long id) {
        if (id == null) return;
        LoginUser currentUser = LoginUserHolder.getLoginUser();
        if (LoginIdentity.isSuperAdmin(currentUser.getUserIdentity())) {
            return;
        }
        SystemUser target = getById(id);
        if (target == null) return;
        boolean otherAdmin = ((target.getType() == SystemUserType.ADMIN || target.getType() == SystemUserType.SUPER_ADMIN) && !Objects.equals(target.getId(), currentUser.getUserId()));
        if(otherAdmin){
            throw new LeaseException(ResultCodeEnum.ADMIN_ACCESS_FORBIDDEN);
        }
    }


}




