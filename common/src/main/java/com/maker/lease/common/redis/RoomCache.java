package com.maker.lease.common.redis;

import com.maker.lease.common.constant.RedisConstant;
import com.maker.lease.model.entity.RoomInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * app端房间详情缓存的统一读写入口
 */
@Component
public class RoomCache {

    private static final String PREFIX = RedisConstant.APP_ROOM_PREFIX;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    // 读
    public <T> T result(Long id) {
        return (T) redisTemplate.opsForValue().get(PREFIX + id);
    }

    // 写（只在缓存未命中、查完库后回填时调用）
    public void put(Long id, Object value, long timeout, TimeUnit unit) {
        redisTemplate.opsForValue().set(PREFIX + id, value, timeout, unit);
    }

    // 删（房间新增/修改/删除时调用）
    public void evict(Long id) {
        redisTemplate.delete(PREFIX + id);
    }

    // 公寓信息更新或删除时，批量删该公寓下所有房间的缓存
    public void evictByApartment(List<RoomInfo> rooms) {
        List<String> keys = rooms.stream()
                .map(room -> PREFIX + room.getId())
                .toList();
        if (!keys.isEmpty()) {
            redisTemplate.delete(keys);
        }
    }

    // 事务提交后再删，避免"缓存已删、事务还没提交"时被其他请求回填旧值
    public void evictAfterCommit(Long id) {
        runAfterCommit(() -> evict(id));
    }

    public void evictByApartmentAfterCommit(List<RoomInfo> rooms) {
        runAfterCommit(() -> evictByApartment(rooms));
    }

    private void runAfterCommit(Runnable action) {
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    action.run();
                }
            });
        } else {
            // 没有事务（例如 controller 直接调用）就立即执行
            action.run();
        }
    }
}
