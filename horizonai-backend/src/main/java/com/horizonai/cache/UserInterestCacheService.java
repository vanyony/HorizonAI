package com.horizonai.cache;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.horizonai.entity.UserInterest;
import com.horizonai.mapper.UserInterestMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserInterestCacheService {

    private static final Logger log = LoggerFactory.getLogger(UserInterestCacheService.class);
    private static final TypeReference<List<Long>> LONG_LIST = new TypeReference<List<Long>>() {};

    private final StringRedisTemplate redisTemplate;
    private final UserInterestMapper userInterestMapper;
    private final ObjectMapper objectMapper;
    private final boolean redisEnabled;
    private final Duration ttl;

    public UserInterestCacheService(
            StringRedisTemplate redisTemplate,
            UserInterestMapper userInterestMapper,
            ObjectMapper objectMapper,
            @Value("${horizon.cache.redis.enabled:true}") boolean redisEnabled,
            @Value("${horizon.cache.interest-ttl:30m}") Duration ttl) {
        this.redisTemplate = redisTemplate;
        this.userInterestMapper = userInterestMapper;
        this.objectMapper = objectMapper;
        this.redisEnabled = redisEnabled;
        this.ttl = ttl;
    }

    public List<Long> getTagIds(Long userId) {
        String key = interestKey(userId);
        if (redisEnabled) {
            try {
                String cached = redisTemplate.opsForValue().get(key);
                if (cached != null) {
                    return objectMapper.readValue(cached, LONG_LIST);
                }
            } catch (Exception e) {
                log.warn("读取用户兴趣缓存失败，回退 MySQL: userId={}, reason={}",
                        userId, e.getMessage());
            }
        }

        List<Long> tagIds = userInterestMapper.selectList(
                new LambdaQueryWrapper<UserInterest>()
                        .eq(UserInterest::getUserId, userId)
        ).stream().map(UserInterest::getTagId).collect(Collectors.toList());

        if (redisEnabled) {
            try {
                redisTemplate.opsForValue().set(
                        key,
                        objectMapper.writeValueAsString(tagIds),
                        ttl
                );
            } catch (Exception e) {
                log.warn("写入用户兴趣缓存失败: userId={}, reason={}", userId, e.getMessage());
            }
        }
        return tagIds;
    }

    public void evictUserProfile(Long userId) {
        if (!redisEnabled) {
            return;
        }
        try {
            redisTemplate.delete(List.of(
                    interestKey(userId),
                    recommendationKey(userId)
            ));
        } catch (Exception e) {
            log.warn("清理用户缓存失败: userId={}, reason={}", userId, e.getMessage());
        }
    }

    public String recommendationKey(Long userId) {
        return "recommend:user:" + userId;
    }

    private String interestKey(Long userId) {
        return "user:interest:" + userId;
    }
}
