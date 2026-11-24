package com.maker.lease.web.admin.mapper;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.maker.lease.model.entity.RoomInfo;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.maker.lease.web.admin.vo.room.RoomItemVo;
import com.maker.lease.web.admin.vo.room.RoomQueryVo;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * @author liubo
 * @description 针对表【room_info(房间信息表)】的数据库操作Mapper
 * @createDate 2023-07-24 15:48:00
 * @Entity com.maker.lease.model.RoomInfo
 */
public interface RoomInfoMapper extends BaseMapper<RoomInfo> {

    IPage<RoomItemVo> pageRoomItemByQuery(IPage<RoomItemVo> page, @Param("queryVo") RoomQueryVo queryVo);

    /**
     * 查询房间并加行锁，必须在事务中调用
     */
    @Select("select * from room_info where id = #{id} and is_deleted = 0 for update")
    RoomInfo selectByIdForUpdate(@Param("id") Long id);
}




