package com.maker.lease.web.app.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.maker.lease.common.login.LoginUserHolder;
import com.maker.lease.common.redis.RoomCache;
import com.maker.lease.model.entity.*;
import com.maker.lease.web.app.mapper.*;
import com.maker.lease.web.app.service.ApartmentInfoService;
import com.maker.lease.web.app.service.BrowsingHistoryService;
import com.maker.lease.web.app.service.RoomInfoService;
import com.maker.lease.web.app.vo.apartment.ApartmentItemVo;
import com.maker.lease.web.app.vo.attr.AttrValueVo;
import com.maker.lease.web.app.vo.fee.FeeValueVo;
import com.maker.lease.web.app.vo.graph.GraphVo;
import com.maker.lease.web.app.vo.room.RoomDetailVo;
import com.maker.lease.web.app.vo.room.RoomItemVo;
import com.maker.lease.web.app.vo.room.RoomQueryVo;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @author liubo
 * @description 针对表【room_info(房间信息表)】的数据库操作Service实现
 * @createDate 2023-07-26 11:12:39
 */
@Service
public class RoomInfoServiceImpl extends ServiceImpl<RoomInfoMapper, RoomInfo>
        implements RoomInfoService {
    @Autowired
    private GraphInfoMapper graphInfoMapper;
    @Autowired
    private AttrValueMapper attrValueMapper;
    @Autowired
    private FacilityInfoMapper facilityInfoMapper;
    @Autowired
    private LabelInfoMapper labelInfoMapper;
    @Autowired
    private PaymentTypeMapper paymentTypeMapper;
    @Autowired
    private FeeValueMapper feeValueMapper;
    @Autowired
    private LeaseTermMapper leaseTermMapper;
    @Autowired
    private ApartmentInfoService apartmentInfoService;
    @Autowired
    private BrowsingHistoryService browsingHistoryService;
    @Autowired
    private RoomCache roomCache;

    @Override
    public IPage<RoomItemVo> pageRoomItemByQuery(Page<RoomItemVo> page, RoomQueryVo queryVo) {
        return baseMapper.pageRoomItemByQuery(page, queryVo);
    }

    @Override
    public RoomDetailVo getRoomDetailById(Long id) {
        // 1.先读缓存
        RoomDetailVo roomDetailVo = roomCache.result(id);
        if (roomDetailVo == null) {
            // 2.缓存未命中，查库
            // 2.1 查询房间信息
            RoomInfo roomInfo = baseMapper.selectById(id);
            if (roomInfo == null) {
                return null;
            }
            Long apartmentId = roomInfo.getApartmentId();

            // 2.2 查询图片列表
            List<GraphVo> graphVoList = graphInfoMapper.getRoomGraphList(id);
            // 2.3 查询属性信息列表
            List<AttrValueVo> attrValueVoList = attrValueMapper.getAttrValueList(id);
            // 2.4 查询配套信息列表
            List<FacilityInfo> facilityInfoList = facilityInfoMapper.getRoomFacilityList(id);
            // 2.5 查询标签信息列表
            List<LabelInfo> labelInfoList = labelInfoMapper.getRoomLabelList(id);
            // 2.6 查询支付方法列表
            List<PaymentType> paymentTypeList = paymentTypeMapper.getRoomPaymentTypeList(id);
            // 2.7 查询杂费信息列表（杂费挂在公寓上）
            List<FeeValueVo> feeValueVoList = feeValueMapper.getApartmentFeeValueVoList(apartmentId);
            // 2.8 查询租期列表
            List<LeaseTerm> leaseTermList = leaseTermMapper.getRoomLeaseTermList(id);
            // 2.9 查询所属公寓信息
            ApartmentItemVo apartmentItemVo = apartmentInfoService.getApartmentItemVo(apartmentId);

            // 2.10 拼接
            roomDetailVo = new RoomDetailVo();
            BeanUtils.copyProperties(roomInfo, roomDetailVo);
            roomDetailVo.setApartmentItemVo(apartmentItemVo);
            roomDetailVo.setGraphVoList(graphVoList);
            roomDetailVo.setAttrValueVoList(attrValueVoList);
            roomDetailVo.setFacilityInfoList(facilityInfoList);
            roomDetailVo.setLabelInfoList(labelInfoList);
            roomDetailVo.setPaymentTypeList(paymentTypeList);
            roomDetailVo.setFeeValueVoList(feeValueVoList);
            roomDetailVo.setLeaseTermList(leaseTermList);

            // 2.11 回填缓存
            roomCache.put(id, roomDetailVo, 20, TimeUnit.MINUTES);
        }

        // 3.记录浏览历史：放在缓存逻辑外面，保证每次请求都执行
        browsingHistoryService.saveHistory(LoginUserHolder.getLoginUser().getUserId(), id);
        return roomDetailVo;
    }

    @Override
    public IPage<RoomItemVo> pageRoomItemByApartmentId(IPage<RoomItemVo> page, Long id) {
        return baseMapper.pageRoomItemByApartmentId(page, id);
    }
}
