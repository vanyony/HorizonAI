package com.horizonai.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.horizonai.ai.observer.ContentEvent;
import com.horizonai.ai.strategy.AnalysisContext;
import com.horizonai.ai.strategy.AnalysisResult;
import com.horizonai.common.BusinessException;
import com.horizonai.entity.AiAnalysisResult;
import com.horizonai.entity.Article;
import com.horizonai.mapper.AiAnalysisResultMapper;
import com.horizonai.mapper.ArticleMapper;
import com.horizonai.service.AiAnalysisService;
import com.horizonai.vo.AiAnalysisVO;
import com.horizonai.cache.TrendCacheService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class AiAnalysisServiceImpl implements AiAnalysisService {

    private static final Logger log = LoggerFactory.getLogger(AiAnalysisServiceImpl.class);

    private final ArticleMapper articleMapper;
    private final AiAnalysisResultMapper aiAnalysisResultMapper;
    private final AnalysisContext analysisContext;
    private final ApplicationEventPublisher eventPublisher;
    private final AiAnalysisPersistenceService persistenceService;
    private final String model;
    private final String promptVersion;
    private final TrendCacheService trendCacheService;

    // ========== 手动构造器（替代 Lombok @RequiredArgsConstructor） ==========

    public AiAnalysisServiceImpl(ArticleMapper articleMapper,
                                  AiAnalysisResultMapper aiAnalysisResultMapper,
                                  AnalysisContext analysisContext,
                                  ApplicationEventPublisher eventPublisher,
                                  AiAnalysisPersistenceService persistenceService,
                                  @Value("${ai.deepseek.model:deepseek-chat}") String model,
                                  @Value("${ai.analysis.prompt-version:v1}") String promptVersion,
                                  TrendCacheService trendCacheService) {
        this.articleMapper = articleMapper;
        this.aiAnalysisResultMapper = aiAnalysisResultMapper;
        this.analysisContext = analysisContext;
        this.eventPublisher = eventPublisher;
        this.persistenceService = persistenceService;
        this.model = model;
        this.promptVersion = promptVersion;
        this.trendCacheService = trendCacheService;
    }

    @Override
    public AiAnalysisVO analyzeArticle(Long articleId) {
        Article article = articleMapper.selectById(articleId);
        if (article == null) {
            throw new BusinessException("文章不存在");
        }

        AiAnalysisResult existing = persistenceService.findExisting(
                articleId,
                article.getContentHash(),
                model,
                promptVersion
        );
        if (existing != null) {
            log.info("分析结果已存在，直接复用: articleId={}, contentHash={}, promptVersion={}",
                    articleId, article.getContentHash(), promptVersion);
            return buildVO(existing, article.getTitle());
        }

        persistenceService.markStatus(articleId, "RUNNING");
        try {
            // 外部模型调用不占用数据库事务。
            AnalysisResult result = analysisContext.analyze(article);
            AiAnalysisResult entity = persistenceService.saveSuccess(
                    article,
                    result,
                    model,
                    promptVersion
            );
            eventPublisher.publishEvent(new ContentEvent(this, article, entity));
            return buildVO(entity, article.getTitle());
        } catch (RuntimeException e) {
            persistenceService.markStatus(articleId, "FAILED");
            throw e;
        }
    }

    @Override
    public AiAnalysisVO getLatestAnalysis(Long articleId) {
        AiAnalysisResult entity = aiAnalysisResultMapper.selectOne(
                new LambdaQueryWrapper<AiAnalysisResult>()
                        .eq(AiAnalysisResult::getArticleId, articleId)
                        .orderByDesc(AiAnalysisResult::getCreatedAt)
                        .last("LIMIT 1")
        );
        if (entity == null) return null;

        Article article = articleMapper.selectById(articleId);
        return buildVO(entity, article != null ? article.getTitle() : "");
    }

    @Override
    public Object getTodayOverview() {
        // 获取最新的 10 篇文章（按重要度排序）
        List<Long> articleIds = trendCacheService.topArticleIds(
                java.time.LocalDate.now(), 10);
        List<Article> articles = trendCacheService.orderedArticles(articleIds);

        List<Map<String, Object>> items = new ArrayList<>();
        for (Article article : articles) {
            AiAnalysisVO analysis = getLatestAnalysis(article.getId());
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("id", article.getId());
            item.put("title", article.getTitle());
            item.put("summary", article.getSummary());
            item.put("sourceType", article.getSourceType());
            item.put("sourceUrl", article.getSourceUrl());
            item.put("importanceRating", article.getImportanceRating());
            item.put("publishDate", article.getPublishDate());
            item.put("analysis", analysis);
            items.add(item);
        }

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("date", java.time.LocalDate.now().toString());
        result.put("totalCount", items.size());
        result.put("items", items);
        return result;
    }

    private AiAnalysisVO buildVO(AiAnalysisResult entity, String articleTitle) {
        return AiAnalysisVO.builder()
                .id(entity.getId())
                .articleId(entity.getArticleId())
                .articleTitle(articleTitle)
                .importanceRating(entity.getImportanceRating())
                .targetAudience(entity.getTargetAudience())
                .industryImpact(entity.getIndustryImpact())
                .learningSuggestions(entity.getLearningSuggestions())
                .summary(entity.getSummary())
                .modelUsed(entity.getModelUsed())
                .createdAt(entity.getCreatedAt())
                .build();
    }
}
