package com.maker.lease.web.app.mapper;

import com.maker.lease.model.entity.RoomInfo;
import com.maker.lease.web.app.vo.room.RoomItemVo;
import com.maker.lease.web.app.vo.room.RoomQueryVo;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Param;

import java.math.BigDecimal;

/**
* @author liubo
* @description 针对表【room_info(房间信息表)】的数据库操作Mapper
* @createDate 2023-07-26 11:12:39
* @Entity com.maker.lease.model.entity.RoomInfo
*/
public interface RoomInfoMapper extends BaseMapper<RoomInfo> {

    IPage<RoomItemVo> pageRoomItemByQuery(Page<RoomItemVo> page, @Param("queryVo") RoomQueryVo queryVo);

    BigDecimal getMinRent(@Param("id") Long id);

    IPage<RoomItemVo> pageRoomItemByApartmentId(IPage<RoomItemVo> page, Long id);
}