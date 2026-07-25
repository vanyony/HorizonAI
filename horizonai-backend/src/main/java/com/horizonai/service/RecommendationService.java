package com.horizonai.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.horizonai.cache.TrendCacheService;
import com.horizonai.cache.UserInterestCacheService;
import com.horizonai.entity.Article;
import com.horizonai.entity.ArticleTag;
import com.horizonai.mapper.ArticleMapper;
import com.horizonai.mapper.ArticleTagMapper;
import com.horizonai.vo.ArticleVO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class RecommendationService {

    private static final Logger log = LoggerFactory.getLogger(RecommendationService.class);
    private static final TypeReference<List<Long>> LONG_LIST = new TypeReference<List<Long>>() {};

    private final UserInterestCacheService interestCacheService;
    private final TrendCacheService trendCacheService;
    private final ArticleTagMapper articleTagMapper;
    private final ArticleMapper articleMapper;
    private final ArticleService articleService;
    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;
    private final boolean redisEnabled;
    private final Duration ttl;

    public RecommendationService(
            UserInterestCacheService interestCacheService,
            TrendCacheService trendCacheService,
            ArticleTagMapper articleTagMapper,
            ArticleMapper articleMapper,
            ArticleService articleService,
            StringRedisTemplate redisTemplate,
            ObjectMapper objectMapper,
            @Value("${horizon.cache.redis.enabled:true}") boolean redisEnabled,
            @Value("${horizon.cache.recommendation-ttl:15m}") Duration ttl) {
        this.interestCacheService = interestCacheService;
        this.trendCacheService = trendCacheService;
        this.articleTagMapper = articleTagMapper;
        this.articleMapper = articleMapper;
        this.articleService = articleService;
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
        this.redisEnabled = redisEnabled;
        this.ttl = ttl;
    }

    public List<ArticleVO> recommend(Long userId, int limit) {
        List<Long> articleIds = readCached(userId);
        if (articleIds == null) {
            articleIds = calculate(userId, limit);
            writeCached(userId, articleIds);
        }
        return articleIds.stream()
                .limit(limit)
                .map(articleService::getById)
                .collect(Collectors.toList());
    }

    private List<Long> calculate(Long userId, int limit) {
        List<Long> tagIds = interestCacheService.getTagIds(userId);
        if (tagIds.isEmpty()) {
            return trendCacheService.topArticleIds(LocalDate.now(), limit);
        }

        List<ArticleTag> relations = articleTagMapper.selectList(
                new LambdaQueryWrapper<ArticleTag>()
                        .in(ArticleTag::getTagId, tagIds)
        );
        if (relations.isEmpty()) {
            return trendCacheService.topArticleIds(LocalDate.now(), limit);
        }

        Map<Long, Integer> matchCounts = new HashMap<>();
        relations.forEach(relation -> matchCounts.merge(
                relation.getArticleId(), 1, Integer::sum));
        List<Article> articles = articleMapper.selectBatchIds(matchCounts.keySet());
        articles.sort(Comparator
                .comparingInt((Article article) -> matchCounts.getOrDefault(article.getId(), 0))
                .thenComparingInt(article -> article.getImportanceRating() == null
                        ? 0
                        : article.getImportanceRating())
                .reversed()
                .thenComparing(Article::getCreatedAt, Comparator.nullsLast(Comparator.reverseOrder())));

        Set<Long> result = articles.stream()
                .map(Article::getId)
                .limit(limit)
                .collect(Collectors.toCollection(LinkedHashSet::new));
        if (result.size() < limit) {
            result.addAll(trendCacheService.topArticleIds(LocalDate.now(), limit));
        }
        return new ArrayList<>(result).stream().limit(limit).collect(Collectors.toList());
    }

    private List<Long> readCached(Long userId) {
        if (!redisEnabled) {
            return null;
        }
        try {
            String value = redisTemplate.opsForValue()
                    .get(interestCacheService.recommendationKey(userId));
            return value == null ? null : objectMapper.readValue(value, LONG_LIST);
        } catch (Exception e) {
            log.warn("读取推荐缓存失败，回退实时计算: userId={}, reason={}",
                    userId, e.getMessage());
            return null;
        }
    }

    private void writeCached(Long userId, List<Long> articleIds) {
        if (!redisEnabled) {
            return;
        }
        try {
            redisTemplate.opsForValue().set(
                    interestCacheService.recommendationKey(userId),
                    objectMapper.writeValueAsString(articleIds),
                    ttl
            );
        } catch (Exception e) {
            log.warn("写入推荐缓存失败: userId={}, reason={}", userId, e.getMessage());
        }
    }
}
