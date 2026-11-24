package com.maker.lease.web.admin.service;

import com.maker.lease.model.entity.LeaseAgreement;
import com.maker.lease.model.enums.LeaseStatus;
import com.maker.lease.web.admin.vo.agreement.AgreementQueryVo;
import com.maker.lease.web.admin.vo.agreement.AgreementVo;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;

/**
* @author liubo
* @description 针对表【lease_agreement(租约信息表)】的数据库操作Service
* @createDate 2023-07-24 15:48:00
*/
public interface LeaseAgreementService extends IService<LeaseAgreement> {

    AgreementVo getAgreementVoById(Long id);

    IPage<AgreementVo> pageAgreementByQuery(IPage<AgreementVo> page, AgreementQueryVo queryVo);

    /**
     * 保存/更新租约，带“一个房间只能有一份生效租约”校验
     */
    void saveOrUpdateAgreement(LeaseAgreement leaseAgreement);

    /**
     * 修改租约状态，带“一个房间只能有一份生效租约”校验
     */
    void updateStatusById(Long id, LeaseStatus status);
}
