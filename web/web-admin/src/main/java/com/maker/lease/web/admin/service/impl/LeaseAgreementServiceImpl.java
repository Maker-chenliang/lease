package com.maker.lease.web.admin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.maker.lease.common.exception.LeaseException;
import com.maker.lease.common.redis.RoomCache;
import com.maker.lease.common.result.ResultCodeEnum;
import com.maker.lease.model.entity.*;
import com.maker.lease.model.enums.LeaseStatus;
import com.maker.lease.web.admin.mapper.*;
import com.maker.lease.web.admin.service.LeaseAgreementService;
import com.maker.lease.web.admin.vo.agreement.AgreementQueryVo;
import com.maker.lease.web.admin.vo.agreement.AgreementVo;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static com.maker.lease.model.enums.LeaseStatus.SIGNING;

/**
 * @author liubo
 * @description 针对表【lease_agreement(租约信息表)】的数据库操作Service实现
 * @createDate 2023-07-24 15:48:00
 */
@Service
public class LeaseAgreementServiceImpl extends ServiceImpl<LeaseAgreementMapper, LeaseAgreement>
        implements LeaseAgreementService {

    /**
     * 生效中的租约状态：签约待确认、已签约、退租待确认、续约待确认
     */
    private static final List<LeaseStatus> ACTIVE_STATUSES = List.of(
            SIGNING, LeaseStatus.SIGNED,
            LeaseStatus.WITHDRAWING, LeaseStatus.RENEWING);

    @Autowired
    private LeaseAgreementMapper leaseAgreementMapper;
    @Autowired
    private ApartmentInfoMapper apartmentInfoMapper;
    @Autowired
    private RoomInfoMapper roomInfoMapper;
    @Autowired
    private PaymentTypeMapper paymentTypeMapper;
    @Autowired
    private LeaseTermMapper leaseTermMapper;
    @Autowired
    private RoomCache roomCache;

    @Override
    public AgreementVo getAgreementVoById(Long id) {
        LeaseAgreement leaseAgreement = leaseAgreementMapper.selectById(id);
        ApartmentInfo apartmentInfo = apartmentInfoMapper.selectById(leaseAgreement.getApartmentId());
        RoomInfo roomInfo = roomInfoMapper.selectById(leaseAgreement.getRoomId());
        PaymentType paymentType = paymentTypeMapper.selectById(leaseAgreement.getPaymentTypeId());
        LeaseTerm leaseTerm = leaseTermMapper.selectById(leaseAgreement.getLeaseTermId());
        AgreementVo agreementVo = new AgreementVo();
        BeanUtils.copyProperties(leaseAgreement, agreementVo);
        agreementVo.setApartmentInfo(apartmentInfo);
        agreementVo.setRoomInfo(roomInfo);
        agreementVo.setPaymentType(paymentType);
        agreementVo.setLeaseTerm(leaseTerm);
        return agreementVo;
    }

    @Override
    public IPage<AgreementVo> pageAgreementByQuery(IPage<AgreementVo> page, AgreementQueryVo queryVo) {
        return leaseAgreementMapper.pageAgreementByQuery(page, queryVo);
    }

    @Override
    @Transactional(rollbackFor = Exception.class, isolation = Isolation.READ_COMMITTED)
    public void saveOrUpdateAgreement(LeaseAgreement leaseAgreement) {
        if (leaseAgreement.getRoomId() == null) {
            throw new LeaseException(ResultCodeEnum.PARAM_ERROR);
        }
        // 锁住房间行：同一房间的租约新增/修改在这里串行执行
        RoomInfo room = roomInfoMapper.selectByIdForUpdate(leaseAgreement.getRoomId());
        if (room == null) {
            throw new LeaseException(ResultCodeEnum.DATA_ERROR);
        }
        LeaseStatus status = leaseAgreement.getStatus();
        if (status == null && leaseAgreement.getId() != null) {
            // 更新时没传状态，以数据库中的状态为准
            LeaseAgreement old = getById(leaseAgreement.getId());
            status = old == null ? null : old.getStatus();
        }
        if (status == null && leaseAgreement.getId() == null) {
            // 防止前端新建时传脏数据
            leaseAgreement.setStatus(SIGNING);
        }
        if (ACTIVE_STATUSES.contains(status)) {
            checkRoomHasNoActiveLease(leaseAgreement.getRoomId(), leaseAgreement.getId());
        }
        saveOrUpdate(leaseAgreement);
        Long apartmentId = leaseAgreement.getApartmentId();
        List<RoomInfo> roomInfos = roomInfoMapper.selectList(new LambdaQueryWrapper<RoomInfo>().eq(RoomInfo::getApartmentId, apartmentId));
        roomCache.evictByApartmentAfterCommit(roomInfos);
    }

    @Override
    @Transactional(rollbackFor = Exception.class, isolation = Isolation.READ_COMMITTED)
    public void updateStatusById(Long id, LeaseStatus status) {
        LeaseAgreement agreement = getById(id);
        if (agreement == null) {
            throw new LeaseException(ResultCodeEnum.DATA_ERROR);
        }
        // 仍然先锁房间行，拿到锁之后再查是否有别的生效租约
        roomInfoMapper.selectByIdForUpdate(agreement.getRoomId());
        if (ACTIVE_STATUSES.contains(status)) {
            checkRoomHasNoActiveLease(agreement.getRoomId(), id);
        }
        LambdaUpdateWrapper<LeaseAgreement> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(LeaseAgreement::getId, id);
        updateWrapper.set(LeaseAgreement::getStatus, status);
        update(updateWrapper);
        Long apartmentId = agreement.getApartmentId();
        List<RoomInfo> roomInfos = roomInfoMapper.selectList(new LambdaQueryWrapper<RoomInfo>().eq(RoomInfo::getApartmentId, apartmentId));
        roomCache.evictByApartmentAfterCommit(roomInfos);
    }

    /**
     * 校验该房间是否已经有别的生效租约，必须有房间行锁才能调用
     */
    private void checkRoomHasNoActiveLease(Long roomId, Long excludeId) {
        Long count = lambdaQuery()
                .eq(LeaseAgreement::getRoomId, roomId)
                .in(LeaseAgreement::getStatus, ACTIVE_STATUSES)
                .ne(excludeId != null, LeaseAgreement::getId, excludeId)
                .count();
        if (count > 0) {
            throw new LeaseException(ResultCodeEnum.APP_ROOM_NOT_AVAILABLE);
        }
    }
}




