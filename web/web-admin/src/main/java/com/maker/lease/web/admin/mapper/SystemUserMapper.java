package com.maker.lease.web.admin.mapper;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.maker.lease.model.entity.SystemUser;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.maker.lease.web.admin.vo.system.user.SystemUserItemVo;
import com.maker.lease.web.admin.vo.system.user.SystemUserQueryVo;

/**
 * @author liubo
 * @description 针对表【system_user(员工信息表)】的数据库操作Mapper
 * @createDate 2023-07-24 15:48:00
 * @Entity com.maker.lease.model.SystemUser
 */
public interface SystemUserMapper extends BaseMapper<SystemUser> {

    IPage<SystemUserItemVo> pageSystemUserByQuery(IPage<SystemUserItemVo> page, SystemUserQueryVo queryVo,Boolean excludeAdmin);
}




