package com.horizonai.cache;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.horizonai.entity.Article;
import com.horizonai.mapper.ArticleMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class TrendCacheService {

    private static final Logger log = LoggerFactory.getLogger(TrendCacheService.class);

    private final StringRedisTemplate redisTemplate;
    private final ArticleMapper articleMapper;
    private final boolean redisEnabled;
    private final Duration ttl;

    public TrendCacheService(
            StringRedisTemplate redisTemplate,
            ArticleMapper articleMapper,
            @Value("${horizon.cache.redis.enabled:true}") boolean redisEnabled,
            @Value("${horizon.cache.trend-ttl:6h}") Duration ttl) {
        this.redisTemplate = redisTemplate;
        this.articleMapper = articleMapper;
        this.redisEnabled = redisEnabled;
        this.ttl = ttl;
    }

    public List<Long> topArticleIds(LocalDate date, int limit) {
        String key = trendKey(date);
        if (redisEnabled) {
            try {
                Set<String> cached = redisTemplate.opsForZSet()
                        .reverseRange(key, 0, Math.max(0, limit - 1));
                if (cached != null && !cached.isEmpty()) {
                    return cached.stream().map(Long::valueOf).collect(Collectors.toList());
                }
            } catch (Exception e) {
                log.warn("读取趋势缓存失败，回退 MySQL: reason={}", e.getMessage());
            }
        }

        List<Article> articles = articleMapper.selectList(
                new LambdaQueryWrapper<Article>()
                        .orderByDesc(Article::getImportanceRating)
                        .orderByDesc(Article::getCreatedAt)
                        .last("LIMIT " + Math.max(1, Math.min(limit, 100)))
        );
        if (redisEnabled) {
            try {
                for (Article article : articles) {
                    recordAnalyzed(article);
                }
                redisTemplate.expire(key, ttl);
            } catch (Exception e) {
                log.warn("回填趋势缓存失败: reason={}", e.getMessage());
            }
        }
        return articles.stream().map(Article::getId).collect(Collectors.toList());
    }

    public void recordAnalyzed(Article article) {
        if (!redisEnabled || article == null || article.getId() == null) {
            return;
        }
        try {
            double score = article.getImportanceRating() == null
                    ? 0D
                    : article.getImportanceRating().doubleValue();
            String key = trendKey(LocalDate.now());
            redisTemplate.opsForZSet().add(key, String.valueOf(article.getId()), score);
            redisTemplate.expire(key, ttl);
        } catch (Exception e) {
            log.warn("更新趋势缓存失败: articleId={}, reason={}",
                    article.getId(), e.getMessage());
        }
    }

    public List<Article> orderedArticles(List<Long> articleIds) {
        if (articleIds.isEmpty()) {
            return new ArrayList<>();
        }
        List<Article> articles = articleMapper.selectBatchIds(articleIds);
        return articleIds.stream()
                .map(id -> articles.stream()
                        .filter(article -> id.equals(article.getId()))
                        .findFirst()
                        .orElse(null))
                .filter(article -> article != null)
                .collect(Collectors.toList());
    }

    private String trendKey(LocalDate date) {
        return "trend:daily:" + date;
    }
}
