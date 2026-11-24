package com.maker.lease.web.admin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.maker.lease.common.redis.RoomCache;
import com.maker.lease.model.entity.*;
import com.maker.lease.model.enums.ItemType;
import com.maker.lease.web.admin.mapper.RoomInfoMapper;
import com.maker.lease.web.admin.service.*;
import com.maker.lease.web.admin.vo.attr.AttrValueVo;
import com.maker.lease.web.admin.vo.graph.GraphVo;
import com.maker.lease.web.admin.vo.room.RoomDetailVo;
import com.maker.lease.web.admin.vo.room.RoomItemVo;
import com.maker.lease.web.admin.vo.room.RoomQueryVo;
import com.maker.lease.web.admin.vo.room.RoomSubmitVo;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * @author liubo
 * @description 针对表【room_info(房间信息表)】的数据库操作Service实现
 * @createDate 2023-07-24 15:48:00
 */
@Service
public class RoomInfoServiceImpl extends ServiceImpl<RoomInfoMapper, RoomInfo>
        implements RoomInfoService {
    @Autowired
    private GraphInfoService graphInfoService;
    @Autowired
    private RoomAttrValueService roomAttrValueService;
    @Autowired
    private RoomFacilityService roomFacilityService;
    @Autowired
    private RoomLabelService roomLabelService;
    @Autowired
    private RoomPaymentTypeService roomPaymentTypeService;
    @Autowired
    private RoomLeaseTermService roomLeaseTermService;
    @Autowired
    private RoomInfoMapper roomInfoMapper;

    @Autowired
    private ApartmentInfoService apartmentInfoService;
    @Autowired
    private AttrValueService attrValueService;
    @Autowired
    private FacilityInfoService facilityInfoService;
    @Autowired
    private LabelInfoService labelInfoService;
    @Autowired
    private PaymentTypeService paymentTypeService;
    @Autowired
    private LeaseTermService leaseTermService;
    @Autowired
    private RoomCache roomCache;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveRoomSubmit(RoomSubmitVo roomSubmitVo) {
        boolean isUpdate = roomSubmitVo.getId() != null;
        super.saveOrUpdate(roomSubmitVo);
        if (isUpdate) {
            //1.删除图片列表
            LambdaQueryWrapper<GraphInfo> graphQueryWrapper = new LambdaQueryWrapper<>();
            graphQueryWrapper.eq(GraphInfo::getItemType, ItemType.ROOM);
            graphQueryWrapper.eq(GraphInfo::getItemId, roomSubmitVo.getId());
            graphInfoService.remove(graphQueryWrapper);
            //2.删除属性信息列表
            LambdaQueryWrapper<RoomAttrValue> roomAttrValueQueryWrapper = new LambdaQueryWrapper<>();
            roomAttrValueQueryWrapper.eq(RoomAttrValue::getRoomId, roomSubmitVo.getId());
            roomAttrValueService.remove(roomAttrValueQueryWrapper);
            //3.删除配套信息列表
            LambdaQueryWrapper<RoomFacility> roomFacilityQueryWrapper = new LambdaQueryWrapper<>();
            roomFacilityQueryWrapper.eq(RoomFacility::getRoomId, roomSubmitVo.getId());
            roomFacilityService.remove(roomFacilityQueryWrapper);
            //4.删除标签信息列表
            LambdaQueryWrapper<RoomLabel> roomLabelQueryWrapper = new LambdaQueryWrapper<>();
            roomLabelQueryWrapper.eq(RoomLabel::getRoomId, roomSubmitVo.getId());
            roomLabelService.remove(roomLabelQueryWrapper);
            //5.删除支付方式列表
            LambdaQueryWrapper<RoomPaymentType> roomPaymentTypeQueryWrapper = new LambdaQueryWrapper<>();
            roomPaymentTypeQueryWrapper.eq(RoomPaymentType::getRoomId, roomSubmitVo.getId());
            roomPaymentTypeService.remove(roomPaymentTypeQueryWrapper);
            //6.删除租期列表
            LambdaQueryWrapper<RoomLeaseTerm> roomLeaseTermQueryWrapper = new LambdaQueryWrapper<>();
            roomLeaseTermQueryWrapper.eq(RoomLeaseTerm::getRoomId, roomSubmitVo.getId());
            roomLeaseTermService.remove(roomLeaseTermQueryWrapper);
            //删除app端的房间数据缓存（租金会有脏数据，要全部删除）
            List<RoomInfo> rooms = roomInfoMapper.selectList(
                    new LambdaQueryWrapper<RoomInfo>()
                            .eq(RoomInfo::getApartmentId, roomSubmitVo.getApartmentId()));
            roomCache.evictByApartmentAfterCommit(rooms);
        }
        //1.添加图片列表
        List<GraphVo> graphVoList = roomSubmitVo.getGraphVoList();
        if (!CollectionUtils.isEmpty(graphVoList)) {
            ArrayList<GraphInfo> graphInfoArrayList = new ArrayList<>();
            for (GraphVo graphVo : graphVoList) {
                GraphInfo graphInfo = new GraphInfo();
                graphInfo.setUrl(graphVo.getUrl());
                graphInfo.setName(graphVo.getName());
                graphInfo.setItemType(ItemType.ROOM);
                graphInfo.setItemId(roomSubmitVo.getId());
                graphInfoArrayList.add(graphInfo);
            }
            graphInfoService.saveBatch(graphInfoArrayList);

        }
        //2.添加属性列表
        List<Long> attrValueIds = roomSubmitVo.getAttrValueIds();
        if (!CollectionUtils.isEmpty(attrValueIds)) {
            ArrayList<RoomAttrValue> roomAttrValueArrayList = new ArrayList<>();
            for (Long id : attrValueIds) {
                RoomAttrValue roomAttrValue = RoomAttrValue.builder().roomId(roomSubmitVo.getId()).attrValueId(id).build();
                roomAttrValueArrayList.add(roomAttrValue);
            }
            roomAttrValueService.saveBatch(roomAttrValueArrayList);
        }
        //3.添加配套信息列表
        List<Long> attrFacilities = roomSubmitVo.getFacilityInfoIds();
        if (!CollectionUtils.isEmpty(attrFacilities)) {
            ArrayList<RoomFacility> roomFacilityArrayList = new ArrayList<>();
            for (Long id : attrFacilities) {
                RoomFacility roomFacility = RoomFacility.builder().roomId(roomSubmitVo.getId()).facilityId(id).build();
                roomFacilityArrayList.add(roomFacility);
            }
            roomFacilityService.saveBatch(roomFacilityArrayList);
        }
        //4.添加标签信息列表
        List<Long> attrLabels = roomSubmitVo.getLabelInfoIds();
        if (!CollectionUtils.isEmpty(attrLabels)) {
            ArrayList<RoomLabel> roomLabelArrayList = new ArrayList<>();
            for (Long id : attrLabels) {
                RoomLabel roomLabel = RoomLabel.builder().roomId(roomSubmitVo.getId()).labelId(id).build();
                roomLabelArrayList.add(roomLabel);
            }
            roomLabelService.saveBatch(roomLabelArrayList);
        }
        //5.添加支付方式列表
        List<Long> paymentTypes = roomSubmitVo.getPaymentTypeIds();
        if (!CollectionUtils.isEmpty(paymentTypes)) {
            ArrayList<RoomPaymentType> roomPaymentTypeArrayList = new ArrayList<>();
            for (Long id : paymentTypes) {
                RoomPaymentType roomPaymentType = RoomPaymentType.builder().roomId(roomSubmitVo.getId()).paymentTypeId(id).build();
                roomPaymentTypeArrayList.add(roomPaymentType);
            }
            roomPaymentTypeService.saveBatch(roomPaymentTypeArrayList);
        }
        //6.添加租期列表
        List<Long> roomLeaseTermList = roomSubmitVo.getLeaseTermIds();
        if (!CollectionUtils.isEmpty(roomLeaseTermList)) {
            ArrayList<RoomLeaseTerm> roomLeaseTermArrayList = new ArrayList<>();
            for (Long id : roomLeaseTermList) {
                RoomLeaseTerm roomLeaseTerm = RoomLeaseTerm.builder().roomId(roomSubmitVo.getId()).leaseTermId(id).build();
                roomLeaseTermArrayList.add(roomLeaseTerm);
            }
            roomLeaseTermService.saveBatch(roomLeaseTermArrayList);
        }

    }

    @Override
    public IPage<RoomItemVo> pageRoomItemByQuery(IPage<RoomItemVo> page, RoomQueryVo queryVo) {
        return roomInfoMapper.pageRoomItemByQuery(page, queryVo);

    }

    @Override
    public RoomDetailVo getRoomDetailById(Long id) {
        //1.获取房间信息
        RoomInfo roomInfo = roomInfoMapper.selectById(id);
        //2.获取公寓信息
        ApartmentInfo apartmentInfo = apartmentInfoService.getById(roomInfo.getApartmentId());
        //3.获取图片列表
        List<GraphVo> graphVoList = graphInfoService.selectListByItemTypeAndId(ItemType.ROOM, id);
        //4.获取属性信息列表
        List<AttrValueVo> attrValueVoList = attrValueService.selectListByRoomId(id);
        //5.获取配套信息列表
        List<FacilityInfo> facilityInfoList = facilityInfoService.selectListByRoomId(id);
        //6.获取标签信息列表
        List<LabelInfo> labelInfoList = labelInfoService.selectListByRoomId(id);
        //7.获取支付方式列表
        List<PaymentType> paymentTypeList = paymentTypeService.selectListByRoomId(id);
        //8.获取租期列表
        List<LeaseTerm> leaseTermList = leaseTermService.selectListByroomId(id);
        //9.拼接
        RoomDetailVo roomDetailVo = new RoomDetailVo();
        BeanUtils.copyProperties(roomInfo, roomDetailVo);
        roomDetailVo.setApartmentInfo(apartmentInfo);
        roomDetailVo.setGraphVoList(graphVoList);
        roomDetailVo.setAttrValueVoList(attrValueVoList);
        roomDetailVo.setFacilityInfoList(facilityInfoList);
        roomDetailVo.setLabelInfoList(labelInfoList);
        roomDetailVo.setPaymentTypeList(paymentTypeList);
        roomDetailVo.setLeaseTermList(leaseTermList);
        return roomDetailVo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void removeRoomById(Long id) {
        RoomInfo room = roomInfoMapper.selectById(id);
        Long apartmentId = room == null ? null : room.getApartmentId();
        List<RoomInfo> rooms = roomInfoMapper.selectList(
                new LambdaQueryWrapper<RoomInfo>()
                        .eq(RoomInfo::getApartmentId, apartmentId));
        //1.删除RoomInfo
        super.removeById(id);

        //2.删除graphInfoList
        LambdaQueryWrapper<GraphInfo> graphQueryWrapper = new LambdaQueryWrapper<>();
        graphQueryWrapper.eq(GraphInfo::getItemType, ItemType.ROOM);
        graphQueryWrapper.eq(GraphInfo::getItemId, id);
        graphInfoService.remove(graphQueryWrapper);

        //3.删除attrValueList
        LambdaQueryWrapper<RoomAttrValue> attrQueryWrapper = new LambdaQueryWrapper<>();
        attrQueryWrapper.eq(RoomAttrValue::getRoomId, id);
        roomAttrValueService.remove(attrQueryWrapper);

        //4.删除facilityInfoList
        LambdaQueryWrapper<RoomFacility> facilityQueryWrapper = new LambdaQueryWrapper<>();
        facilityQueryWrapper.eq(RoomFacility::getRoomId, id);
        roomFacilityService.remove(facilityQueryWrapper);

        //5.删除labelInfoList
        LambdaQueryWrapper<RoomLabel> labelQueryWrapper = new LambdaQueryWrapper<>();
        labelQueryWrapper.eq(RoomLabel::getRoomId, id);
        roomLabelService.remove(labelQueryWrapper);

        //6.删除paymentTypeList
        LambdaQueryWrapper<RoomPaymentType> paymentQueryWrapper = new LambdaQueryWrapper<>();
        paymentQueryWrapper.eq(RoomPaymentType::getRoomId, id);
        roomPaymentTypeService.remove(paymentQueryWrapper);

        //7.删除leaseTermList
        LambdaQueryWrapper<RoomLeaseTerm> termQueryWrapper = new LambdaQueryWrapper<>();
        termQueryWrapper.eq(RoomLeaseTerm::getRoomId, id);
        roomLeaseTermService.remove(termQueryWrapper);

        //删除app端的房间数据缓存（租金会有脏数据，要全部删除）
        roomCache.evictByApartmentAfterCommit(rooms);

    }
}




