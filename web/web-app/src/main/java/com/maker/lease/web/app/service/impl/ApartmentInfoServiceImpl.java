package com.maker.lease.web.app.service.impl;

import com.maker.lease.model.entity.ApartmentInfo;
import com.maker.lease.model.entity.FacilityInfo;
import com.maker.lease.model.entity.LabelInfo;
import com.maker.lease.web.app.mapper.*;
import com.maker.lease.web.app.service.ApartmentInfoService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.maker.lease.web.app.vo.apartment.ApartmentDetailVo;
import com.maker.lease.web.app.vo.apartment.ApartmentItemVo;
import com.maker.lease.web.app.vo.graph.GraphVo;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

/**
 * @author liubo
 * @description 针对表【apartment_info(公寓信息表)】的数据库操作Service实现
 * @createDate 2023-07-26 11:12:39
 */
@Service
public class ApartmentInfoServiceImpl extends ServiceImpl<ApartmentInfoMapper, ApartmentInfo>
        implements ApartmentInfoService {
    @Autowired
    private ApartmentInfoMapper apartmentInfoMapper;
    @Autowired
    private LabelInfoMapper labelInfoMapper;
    @Autowired
    private GraphInfoMapper graphInfoMapper;
    @Autowired
    private RoomInfoMapper roomInfoMapper;
    @Autowired
    private FacilityInfoMapper facilityInfoMapper;
    @Override
    public ApartmentItemVo getApartmentItemVo(Long id) {
        ApartmentInfo apartmentInfo = apartmentInfoMapper.selectById(id);
        if (apartmentInfo == null) {
            return null;
        }

        List<LabelInfo> labelInfoList = labelInfoMapper.getApartmentLabelList(id);
        List<GraphVo> graphVoList = graphInfoMapper.getApartmentGraphList(id);
        BigDecimal minRent = roomInfoMapper.getMinRent(id);

        ApartmentItemVo apartmentItemVo = new ApartmentItemVo();
        BeanUtils.copyProperties(apartmentInfo, apartmentItemVo);
        apartmentItemVo.setGraphVoList(graphVoList);
        apartmentItemVo.setLabelInfoList(labelInfoList);
        apartmentItemVo.setMinRent(minRent);
        return apartmentItemVo;
    }

    @Override
    public ApartmentDetailVo getApartmentDetailById(Long id) {
        ApartmentInfo apartmentInfo = apartmentInfoMapper.selectById(id);
        if (apartmentInfo == null) {
            return null;
        }
        List<LabelInfo> labelInfoList = labelInfoMapper.getApartmentLabelList(id);
        List<GraphVo> graphVoList = graphInfoMapper.getApartmentGraphList(id);
        List<FacilityInfo> facilityInfoList=facilityInfoMapper.getApartmentList(id);
        BigDecimal minRent = roomInfoMapper.getMinRent(id);

        ApartmentDetailVo apartmentDetailVo = new ApartmentDetailVo();
        BeanUtils.copyProperties(apartmentInfo, apartmentDetailVo);
        apartmentDetailVo.setGraphVoList(graphVoList);
        apartmentDetailVo.setLabelInfoList(labelInfoList);
        apartmentDetailVo.setFacilityInfoList(facilityInfoList);
        apartmentDetailVo.setMinRent(minRent);
        return apartmentDetailVo;
    }
}
