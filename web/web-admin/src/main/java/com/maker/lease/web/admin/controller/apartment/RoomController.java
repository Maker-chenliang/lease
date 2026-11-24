package com.maker.lease.web.admin.controller.apartment;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.maker.lease.common.constant.RedisConstant;
import com.maker.lease.common.redis.RoomCache;
import com.maker.lease.common.result.Result;
import com.maker.lease.model.entity.RoomInfo;
import com.maker.lease.model.enums.ReleaseStatus;
import com.maker.lease.web.admin.service.RoomInfoService;
import com.maker.lease.web.admin.vo.room.RoomDetailVo;
import com.maker.lease.web.admin.vo.room.RoomItemVo;
import com.maker.lease.web.admin.vo.room.RoomQueryVo;
import com.maker.lease.web.admin.vo.room.RoomSubmitVo;
import com.baomidou.mybatisplus.core.metadata.IPage;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "房间信息管理")
@RestController
@RequestMapping("/admin/room")
public class RoomController {
    @Autowired
    private RoomInfoService roomInfoService;
    @Autowired
    private RoomCache roomCache;

    @Operation(summary = "保存或更新房间信息")
    @PostMapping("saveOrUpdate")
    public Result saveOrUpdate(@RequestBody RoomSubmitVo roomSubmitVo) {
        roomInfoService.saveRoomSubmit(roomSubmitVo);
        return Result.ok();
    }

    @Operation(summary = "根据条件分页查询房间列表")
    @GetMapping("pageItem")
    public Result<IPage<RoomItemVo>> pageItem(@RequestParam long current, @RequestParam long size, RoomQueryVo queryVo) {
        IPage<RoomItemVo> page=new Page<>(current, size);
        IPage<RoomItemVo> result=roomInfoService.pageRoomItemByQuery(page,queryVo);
        return Result.ok(result);
    }

    @Operation(summary = "根据id获取房间详细信息")
    @GetMapping("getDetailById")
    public Result<RoomDetailVo> getDetailById(@RequestParam Long id) {
        RoomDetailVo roomDetailVo= roomInfoService.getRoomDetailById(id);
        return Result.ok(roomDetailVo);
    }

    @Operation(summary = "根据id删除房间信息")
    @DeleteMapping("removeById")
    public Result removeById(@RequestParam Long id) {
        roomInfoService.removeRoomById(id);
        return Result.ok();
    }

    @Operation(summary = "根据id修改房间发布状态")
    @PostMapping("updateReleaseStatusById")
    public Result updateReleaseStatusById(Long id, ReleaseStatus status) {
        LambdaUpdateWrapper<RoomInfo> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(RoomInfo::getId, id);
        updateWrapper.set(RoomInfo::getIsRelease, status);
        roomInfoService.update(updateWrapper);
        //删除app端的房间数据缓存（租金会有脏数据）
        RoomInfo room = roomInfoService.getById(id);
        List<RoomInfo> rooms = roomInfoService.list(
                new LambdaQueryWrapper<RoomInfo>().eq(RoomInfo::getApartmentId, room.getApartmentId()));
        roomCache.evictByApartment(rooms);
        return Result.ok();
    }

    @GetMapping("listBasicByApartmentId")
    @Operation(summary = "根据公寓id查询房间列表")
    public Result<List<RoomInfo>> listBasicByApartmentId(Long id) {
        LambdaQueryWrapper<RoomInfo> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(RoomInfo::getApartmentId, id);
        queryWrapper.eq(RoomInfo::getIsRelease, ReleaseStatus.RELEASED);
        List<RoomInfo> roomInfoList = roomInfoService.list(queryWrapper);
        return Result.ok(roomInfoList);
    }

}


















