package com.maker.lease.common.redis;

import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.RedisSerializer;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableCaching
public class RedisConfiguration {
    // RedisTemplate 配置
    @Bean
    public RedisTemplate<String, Object> stringObjectRedisTemplate(RedisConnectionFactory redisConnectionFactory) {
        RedisTemplate<String, Object> stringObjectRedisTemplate = new RedisTemplate<>();
        stringObjectRedisTemplate.setConnectionFactory(redisConnectionFactory);
        // key用字符串
        stringObjectRedisTemplate.setKeySerializer(RedisSerializer.string());
        stringObjectRedisTemplate.setHashKeySerializer(RedisSerializer.string());
        // value用json
        GenericJackson2JsonRedisSerializer jsonRedisSerializer = new GenericJackson2JsonRedisSerializer();
        stringObjectRedisTemplate.setValueSerializer(jsonRedisSerializer);
        stringObjectRedisTemplate.setHashValueSerializer(jsonRedisSerializer);
        return stringObjectRedisTemplate;

    }

    // @Cacheable 注解缓存配置
    @Bean
    public RedisCacheManager cacheManager(RedisConnectionFactory factory) {
        RedisCacheConfiguration config = RedisCacheConfiguration.defaultCacheConfig()
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(new GenericJackson2JsonRedisSerializer()))
                .entryTtl(Duration.ofMinutes(30));
        Map<String, RedisCacheConfiguration> configs = new HashMap<>();
        // 给特定缓存名单独定制过期时间
        configs.put("admin:province", config.entryTtl(Duration.ofHours(24)));
        configs.put("admin:paymentType", config.entryTtl(Duration.ofHours(24)));
        return RedisCacheManager.builder(factory)
                .cacheDefaults(config) //  所有缓存的默认规则：30 分钟
                .withInitialCacheConfigurations(configs).build(); // 名单内缓存用专属规则：24 小时
    }
}
