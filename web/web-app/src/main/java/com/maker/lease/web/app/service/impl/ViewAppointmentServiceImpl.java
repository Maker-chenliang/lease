package com.maker.lease.web.app.service.impl;

import com.maker.lease.model.entity.BaseEntity;
import com.maker.lease.model.entity.RoomInfo;
import com.maker.lease.model.entity.ViewAppointment;
import com.maker.lease.web.app.mapper.ApartmentInfoMapper;
import com.maker.lease.web.app.mapper.GraphInfoMapper;
import com.maker.lease.web.app.mapper.LabelInfoMapper;
import com.maker.lease.web.app.mapper.ViewAppointmentMapper;
import com.maker.lease.web.app.service.ApartmentInfoService;
import com.maker.lease.web.app.service.RoomInfoService;
import com.maker.lease.web.app.service.ViewAppointmentService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.maker.lease.web.app.vo.apartment.ApartmentItemVo;
import com.maker.lease.web.app.vo.appointment.AppointmentDetailVo;
import com.maker.lease.web.app.vo.appointment.AppointmentItemVo;
import com.maker.lease.web.app.vo.room.RoomItemVo;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author liubo
 * @description 针对表【view_appointment(预约看房信息表)】的数据库操作Service实现
 * @createDate 2023-07-26 11:12:39
 */
@Service
public class ViewAppointmentServiceImpl extends ServiceImpl<ViewAppointmentMapper, ViewAppointment>
        implements ViewAppointmentService {
    @Autowired
    private ViewAppointmentMapper mapper;
    @Autowired
    private ApartmentInfoService apartmentInfoService;
    @Autowired
    private RoomInfoService roomInfoService;
    @Autowired
    private GraphInfoMapper graphInfoMapper;
    @Autowired
    private LabelInfoMapper labelInfoMapper;
    @Override
    public List<AppointmentItemVo> getAppointmentItemVo(Long userId) {
        return mapper.getAppointmentItemVo(userId);
    }

    @Override
    public AppointmentDetailVo getAppointmentDetailById(Long id) {
        ViewAppointment viewAppointment = mapper.selectById(id);
        if (viewAppointment == null) {
            return null;
        }
        ApartmentItemVo apartmentItemVo = apartmentInfoService.getApartmentItemVo(viewAppointment.getApartmentId());
        AppointmentDetailVo appointmentDetailVo = new AppointmentDetailVo();
        BeanUtils.copyProperties(viewAppointment,appointmentDetailVo);
        appointmentDetailVo.setApartmentItemVo(apartmentItemVo);
        // 预约的房间信息
        if (viewAppointment.getRoomId() != null) {
            RoomInfo roomInfo = roomInfoService.getById(viewAppointment.getRoomId());
            if (roomInfo != null) {
                RoomItemVo roomItemVo = new RoomItemVo();
                BeanUtils.copyProperties(roomInfo, roomItemVo);
                roomItemVo.setGraphVoList(graphInfoMapper.getRoomGraphList(roomInfo.getId()));
                roomItemVo.setLabelInfoList(labelInfoMapper.getRoomLabelList(roomInfo.getId()));
                roomItemVo.setApartmentInfo(apartmentInfoService.getById(roomInfo.getApartmentId()));
                appointmentDetailVo.setRoomItemVo(roomItemVo);
            }
        }
        return appointmentDetailVo;
    }
}




