package com.example.usermanager.config;

import com.example.usermanager.utils.convert.BytesToLocalDateConvert;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.convert.RedisCustomConversions;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;

import java.time.Duration;
import java.util.List;

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
                .entryTtl(Duration.ofSeconds(300)) // Thiết lập TTL cho mục cache ( 300 giây)
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(new GenericJackson2JsonRedisSerializer()));

        return RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(cacheConfig)
                .build();
    }

    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);
        //
        return template;
    }

    @Bean
    public RedisCustomConversions redisCustomConversions(BytesToLocalDateConvert bytesToLocalDateTimeConverter) {
        return new RedisCustomConversions(List.of(bytesToLocalDateTimeConverter));
    }

}
