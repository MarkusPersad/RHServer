package org.markus.rhserver.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.babyfish.jimmer.meta.ImmutableProp;
import org.babyfish.jimmer.meta.ImmutableType;
import org.babyfish.jimmer.spring.cache.RedisCacheCreator;
import org.babyfish.jimmer.sql.cache.AbstractCacheFactory;
import org.babyfish.jimmer.sql.cache.Cache;
import org.babyfish.jimmer.sql.cache.CacheCreator;
import org.babyfish.jimmer.sql.cache.CacheFactory;
import org.babyfish.jimmer.sql.cache.redisson.RedissonCacheLocker;
import org.babyfish.jimmer.sql.cache.redisson.RedissonCacheTracker;
import org.redisson.api.RedissonClient;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;

import java.time.Duration;
import java.util.List;

@ConditionalOnProperty("spring.data.redis.host")
@Configuration
class CacheConfiguration {

    @Bean
    public CacheFactory cacheFactory(
            RedissonClient redissonClient,
            RedisConnectionFactory factory,
            ObjectMapper objectMapper
    ){
        CacheCreator cacheCreator = new RedisCacheCreator(factory,objectMapper)
                .withRemoteDuration(Duration.ofHours(1))
                .withLocalCache(100,Duration.ofMinutes(5))
                .withMultiViewProperties(40,Duration.ofMinutes(2),Duration.ofMinutes(24))
                .withSoftLock(
                        new RedissonCacheLocker(redissonClient),
                        Duration.ofSeconds(30)
                )
                .withTracking(
                        new RedissonCacheTracker(redissonClient)
                );
        return new AbstractCacheFactory() {
            @Override
            public Cache<?, ?> createObjectCache(ImmutableType type) {
                return cacheCreator.createForObject(type);
            }

            @Override
            public Cache<?, ?> createAssociatedIdCache(ImmutableProp prop) {
                return cacheCreator.createForProp(prop,getFilterState().isAffected(prop.getTargetType()));
            }

            @Override
            public Cache<?, List<?>> createAssociatedIdListCache(ImmutableProp prop) {
                return cacheCreator.createForProp(prop,getFilterState().isAffected(prop.getTargetType()));
            }

            @Override
            public Cache<?, ?> createResolverCache(ImmutableProp prop) {
                return cacheCreator.createForProp(prop,true);
            }
        };
    }
}
