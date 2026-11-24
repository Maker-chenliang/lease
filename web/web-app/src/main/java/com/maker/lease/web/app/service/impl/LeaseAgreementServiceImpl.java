package com.maker.lease.web.app.service.impl;

import com.maker.lease.model.entity.ApartmentInfo;
import com.maker.lease.model.entity.LeaseAgreement;
import com.maker.lease.model.entity.LeaseTerm;
import com.maker.lease.model.entity.PaymentType;
import com.maker.lease.model.entity.RoomInfo;
import com.maker.lease.web.app.mapper.ApartmentInfoMapper;
import com.maker.lease.web.app.mapper.GraphInfoMapper;
import com.maker.lease.web.app.mapper.LeaseAgreementMapper;
import com.maker.lease.web.app.mapper.LeaseTermMapper;
import com.maker.lease.web.app.mapper.PaymentTypeMapper;
import com.maker.lease.web.app.mapper.RoomInfoMapper;
import com.maker.lease.web.app.service.LeaseAgreementService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.maker.lease.web.app.vo.agreement.AgreementDetailVo;
import com.maker.lease.web.app.vo.agreement.AgreementItemVo;
import com.maker.lease.web.app.vo.graph.GraphVo;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author liubo
 * @description 针对表【lease_agreement(租约信息表)】的数据库操作Service实现
 * @createDate 2023-07-26 11:12:39
 */
@Service
public class LeaseAgreementServiceImpl extends ServiceImpl<LeaseAgreementMapper, LeaseAgreement>
        implements LeaseAgreementService {
    @Autowired
    private LeaseAgreementMapper leaseAgreementMapper;
    @Autowired
    private ApartmentInfoMapper apartmentInfoMapper;
    @Autowired
    private RoomInfoMapper roomInfoMapper;
    @Autowired
    private GraphInfoMapper graphInfoMapper;
    @Autowired
    private PaymentTypeMapper paymentTypeMapper;
    @Autowired
    private LeaseTermMapper leaseTermMapper;

    @Override
    public List<AgreementItemVo> getAgreementItemList(String phone) {
        return leaseAgreementMapper.getAgreementItemList(phone);
    }

    @Override
    public AgreementDetailVo getDetailById(Long id) {
        // 1.查询租约信息
        LeaseAgreement leaseAgreement = leaseAgreementMapper.selectById(id);
        if (leaseAgreement == null) {
            return null;
        }
        // 2.查询公寓信息
        ApartmentInfo apartmentInfo = apartmentInfoMapper.selectById(leaseAgreement.getApartmentId());

        // 3.查询房间信息
        RoomInfo roomInfo = roomInfoMapper.selectById(leaseAgreement.getRoomId());

        // 4.查询图片列表
        List<GraphVo> roomGraphVoList = graphInfoMapper.getRoomGraphList(leaseAgreement.getRoomId());
        List<GraphVo> apartmentGraphVoList = graphInfoMapper.getApartmentGraphList(leaseAgreement.getApartmentId());

        // 5.查询支付方式
        PaymentType paymentType = paymentTypeMapper.selectById(leaseAgreement.getPaymentTypeId());

        // 6.查询租期
        LeaseTerm leaseTerm = leaseTermMapper.selectById(leaseAgreement.getLeaseTermId());

        AgreementDetailVo agreementDetailVo = new AgreementDetailVo();
        BeanUtils.copyProperties(leaseAgreement, agreementDetailVo);
        agreementDetailVo.setApartmentName(apartmentInfo.getName());
        agreementDetailVo.setRoomNumber(roomInfo.getRoomNumber());
        agreementDetailVo.setApartmentGraphVoList(apartmentGraphVoList);
        agreementDetailVo.setRoomGraphVoList(roomGraphVoList);
        agreementDetailVo.setPaymentTypeName(paymentType.getName());
        agreementDetailVo.setLeaseTermMonthCount(leaseTerm.getMonthCount());
        agreementDetailVo.setLeaseTermUnit(leaseTerm.getUnit());

        return agreementDetailVo;
    }
}
