package com.example.usermanager.config;

import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;

import java.time.Duration;

@Configuration
@EnableCaching
public class CacheConfig {

//    @Bean
//    public CacheManager cacheManager() {
//        CompositeCacheManager compositeCacheManager = new CompositeCacheManager();
//
//        ConcurrentMapCacheManager customerCacheManager = new ConcurrentMapCacheManager("customerResponse");
////        ConcurrentMapCacheManager userCacheManager = new ConcurrentMapCacheManager("customerResponse");
//
//        // Thêm các CacheManager vào CompositeCacheManager
//        compositeCacheManager.setCacheManagers(List.of(customerCacheManager));
//        compositeCacheManager.setFallbackToNoOpCache(true); //để tránh lỗi nếu không có CacheManager nào khớp
//
//        return compositeCacheManager;
//    }

    @Bean
    public RedisCacheManager cacheManager(RedisConnectionFactory connectionFactory) {
        RedisCacheConfiguration cacheConfig = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofSeconds(300)) // Thiết lập TTL cho mục cache (ví dụ: 300 giây)
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(new GenericJackson2JsonRedisSerializer()));

        return RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(cacheConfig)
                .build();
    }

}
