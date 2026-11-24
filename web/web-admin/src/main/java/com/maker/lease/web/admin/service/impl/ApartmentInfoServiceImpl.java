package com.maker.lease.web.admin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.maker.lease.common.constant.RedisConstant;
import com.maker.lease.common.exception.LeaseException;
import com.maker.lease.common.redis.RoomCache;
import com.maker.lease.common.result.ResultCodeEnum;
import com.maker.lease.model.entity.*;
import com.maker.lease.model.enums.ItemType;
import com.maker.lease.web.admin.mapper.*;
import com.maker.lease.web.admin.service.*;
import com.maker.lease.web.admin.vo.apartment.ApartmentDetailVo;
import com.maker.lease.web.admin.vo.apartment.ApartmentItemVo;
import com.maker.lease.web.admin.vo.apartment.ApartmentQueryVo;
import com.maker.lease.web.admin.vo.apartment.ApartmentSubmitVo;
import com.maker.lease.web.admin.vo.fee.FeeValueVo;
import com.maker.lease.web.admin.vo.graph.GraphVo;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * @author liubo
 * @description 针对表【apartment_info(公寓信息表)】的数据库操作Service实现
 * @createDate 2023-07-24 15:48:00
 */
@Service
public class ApartmentInfoServiceImpl extends ServiceImpl<ApartmentInfoMapper, ApartmentInfo>
        implements ApartmentInfoService {
    @Autowired
    private ApartmentInfoMapper mapper;
    @Autowired
    private GraphInfoMapper graphInfoMapper;
    @Autowired
    private LabelInfoMapper labelInfoMapper;
    @Autowired
    private FacilityInfoMapper facilityInfoMapper;
    @Autowired
    private FeeValueMapper feeValueMapper;
    @Autowired
    private RoomInfoMapper roomInfoMapper;
    @Autowired
    private GraphInfoService graphInfoService;
    @Autowired
    private ApartmentFacilityService apartmentFacilityService;
    @Autowired
    private ApartmentLabelService apartmentLabelService;
    @Autowired
    private ApartmentFeeValueService apartmentFeeValueService;
    @Autowired
    private RoomCache roomCache;

    @Override
    public IPage<ApartmentItemVo> pageItem(IPage<ApartmentItemVo> iPage, ApartmentQueryVo queryVo) {
        return mapper.pageItem(iPage, queryVo);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveOrUpdateApartment(ApartmentSubmitVo apartmentSubmitVo) {
        boolean isUpdate = apartmentSubmitVo.getId() != null;
        super.saveOrUpdate(apartmentSubmitVo);
        if (isUpdate) {
            // 1.删除图片列表
            LambdaQueryWrapper<GraphInfo> graphQueryWrapper = new LambdaQueryWrapper<>();
            graphQueryWrapper.eq(GraphInfo::getItemType, ItemType.APARTMENT).eq(GraphInfo::getItemId, apartmentSubmitVo.getId());
            graphInfoService.remove(graphQueryWrapper);
            // 2.删除配套信息列表
            LambdaQueryWrapper<ApartmentFacility> facilityQueryWrapper = new LambdaQueryWrapper<>();
            facilityQueryWrapper.eq(ApartmentFacility::getApartmentId,apartmentSubmitVo.getId());
            apartmentFacilityService.remove(facilityQueryWrapper);
            // 3.删除标签列表
            LambdaQueryWrapper<ApartmentLabel> apartmentLabeQueryWrapper = new LambdaQueryWrapper<>();
            apartmentLabeQueryWrapper.eq(ApartmentLabel::getApartmentId,apartmentSubmitVo.getId());
            apartmentLabelService.remove(apartmentLabeQueryWrapper);
            // 4.删除杂费列表
            LambdaQueryWrapper<ApartmentFeeValue> apartmentFeeValueQueryWrapper = new LambdaQueryWrapper<>();
            apartmentFeeValueQueryWrapper.eq(ApartmentFeeValue::getApartmentId,apartmentSubmitVo.getId());
            apartmentFeeValueService.remove(apartmentFeeValueQueryWrapper);


        }
        // 1.插入图片列表
        List<GraphVo> graphVoList=apartmentSubmitVo.getGraphVoList();
        if(!CollectionUtils.isEmpty(graphVoList)){
            ArrayList<GraphInfo> graphInfoList=new ArrayList<>();
            for(GraphVo graphVo:graphVoList){
                GraphInfo graphInfo=new GraphInfo();
                graphInfo.setName(graphVo.getName());
                graphInfo.setItemType(ItemType.APARTMENT);
                graphInfo.setItemId(apartmentSubmitVo.getId());
                graphInfo.setUrl(graphVo.getUrl());
                graphInfoList.add(graphInfo);
            }
            graphInfoService.saveBatch(graphInfoList);
        }
        // 2.插入配套信息列表
        List<Long> ApartmentFacilityList=apartmentSubmitVo.getFacilityInfoIds();
        if(!CollectionUtils.isEmpty(ApartmentFacilityList)){
            ArrayList<ApartmentFacility> facilityList=new ArrayList<>();
            for(Long facilityId:ApartmentFacilityList){
                ApartmentFacility apartmentFacility = new ApartmentFacility();
                apartmentFacility.setApartmentId(apartmentSubmitVo.getId());
                apartmentFacility.setFacilityId(facilityId);
                facilityList.add(apartmentFacility);
            }
            apartmentFacilityService.saveBatch(facilityList);
        }
        // 3.插入标签列表
        List<Long> ApartmentLableList=apartmentSubmitVo.getLabelIds();
        if(!CollectionUtils.isEmpty(ApartmentLableList)){
            ArrayList<ApartmentLabel> apartmentLabelList=new ArrayList<>();
            for(Long lableId:ApartmentLableList){
                ApartmentLabel apartmentLabel=new ApartmentLabel();
                apartmentLabel.setApartmentId(apartmentSubmitVo.getId());
                apartmentLabel.setLabelId(lableId);
                apartmentLabelList.add(apartmentLabel);

            }
            apartmentLabelService.saveBatch(apartmentLabelList);
        }
        // 4.插入杂费列表
        List<Long> apartmentFeeValueIds=apartmentSubmitVo.getFeeValueIds();
        if(!CollectionUtils.isEmpty(apartmentFeeValueIds)){
            ArrayList<ApartmentFeeValue> apartmentFeeValueList=new ArrayList<>();
            for(Long feeValueId:apartmentFeeValueIds){
                ApartmentFeeValue apartmentFeeValue=new ApartmentFeeValue();
                apartmentFeeValue.setApartmentId(apartmentSubmitVo.getId());
                apartmentFeeValue.setFeeValueId(feeValueId);
                apartmentFeeValueList.add(apartmentFeeValue);
            }
            apartmentFeeValueService.saveBatch(apartmentFeeValueList);
        }
        // 删除app端的房间数据
        List<RoomInfo> roomInfos = roomInfoMapper.selectList(new LambdaQueryWrapper<RoomInfo>().eq(RoomInfo::getApartmentId, apartmentSubmitVo.getId()));
        roomCache.evictByApartmentAfterCommit(roomInfos);

    }

    @Override
    public ApartmentDetailVo getDetailById(Long id) {
        //1.查询公寓信息
        ApartmentInfo apartmentInfo = mapper.selectById(id);
        //2.查询图片列表
        List<GraphVo> graphInfoList= graphInfoMapper.selectListByItemTypeAndId(ItemType.APARTMENT,id);
        //3.查询标签列表
        List<LabelInfo> labelInfoList=labelInfoMapper.selectListByApartmentId(id);
        //4.查询配套列表
        List<FacilityInfo> facilityInfoList=facilityInfoMapper.selectListByApartmentId(id);
        //5.查询杂费列表
        List<FeeValueVo> feeValueVoList = feeValueMapper.selectListByApartmentId(id);
        //6.拼接
        ApartmentDetailVo apartmentDetailVo=new ApartmentDetailVo();
        BeanUtils.copyProperties(apartmentInfo,apartmentDetailVo);
        apartmentDetailVo.setGraphVoList(graphInfoList);
        apartmentDetailVo.setLabelInfoList(labelInfoList);
        apartmentDetailVo.setFacilityInfoList(facilityInfoList);
        apartmentDetailVo.setFeeValueVoList(feeValueVoList);
        return apartmentDetailVo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void removeApartmentById(Long id) {
        LambdaQueryWrapper<RoomInfo> roomInfoWrapper = new LambdaQueryWrapper<>();
        roomInfoWrapper.eq(RoomInfo::getApartmentId,id);
        Long count = roomInfoMapper.selectCount(roomInfoWrapper);
        if(count>0){
            throw new LeaseException(ResultCodeEnum.ADMIN_APARTMENT_DELETE_ERROR);
        }

        super.removeById(id);
        //1. 删除图片信息
        LambdaQueryWrapper<GraphInfo> graphInfoWrapper=new LambdaQueryWrapper<>();
        graphInfoWrapper.eq(GraphInfo::getItemType,ItemType.APARTMENT).eq(GraphInfo::getItemId,id);
        graphInfoService.remove(graphInfoWrapper);
        //2.删除配套信息
        LambdaQueryWrapper<ApartmentFacility> apartmentFacilityWrapper=new LambdaQueryWrapper<>();
        apartmentFacilityWrapper.eq(ApartmentFacility::getApartmentId,id);
        apartmentFacilityService.remove(apartmentFacilityWrapper);
        //3.删除标签信息
        LambdaQueryWrapper<ApartmentLabel> apartmentLabelWrapper = new LambdaQueryWrapper<>();
        apartmentLabelWrapper.eq(ApartmentLabel::getApartmentId,id);
        apartmentLabelService.remove(apartmentLabelWrapper);
        //4.删除杂费信息
        LambdaQueryWrapper<ApartmentFeeValue> apartmentFeeValueWrapper = new LambdaQueryWrapper<>();
        apartmentFeeValueWrapper.eq(ApartmentFeeValue::getApartmentId,id);
        apartmentFeeValueService.remove(apartmentFeeValueWrapper);
    }
}


