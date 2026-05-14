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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
public class AiAnalysisServiceImpl implements AiAnalysisService {

    private static final Logger log = LoggerFactory.getLogger(AiAnalysisServiceImpl.class);

    private final ArticleMapper articleMapper;
    private final AiAnalysisResultMapper aiAnalysisResultMapper;
    private final AnalysisContext analysisContext;
    private final ApplicationEventPublisher eventPublisher;

    // ========== 手动构造器（替代 Lombok @RequiredArgsConstructor） ==========

    public AiAnalysisServiceImpl(ArticleMapper articleMapper,
                                  AiAnalysisResultMapper aiAnalysisResultMapper,
                                  AnalysisContext analysisContext,
                                  ApplicationEventPublisher eventPublisher) {
        this.articleMapper = articleMapper;
        this.aiAnalysisResultMapper = aiAnalysisResultMapper;
        this.analysisContext = analysisContext;
        this.eventPublisher = eventPublisher;
    }

    @Override
    @Transactional
    public AiAnalysisVO analyzeArticle(Long articleId) {
        Article article = articleMapper.selectById(articleId);
        if (article == null) {
            throw new BusinessException("文章不存在");
        }

        // 执行 AI 分析（工厂 + 策略模式协作）
        AnalysisResult result = analysisContext.analyze(article);

        // 保存分析结果
        AiAnalysisResult entity = new AiAnalysisResult();
        entity.setArticleId(articleId);
        entity.setImportanceRating(result.getImportanceRating());
        entity.setTargetAudience(result.getTargetAudience());
        entity.setIndustryImpact(result.getIndustryImpact());
        entity.setLearningSuggestions(result.getLearningSuggestions());
        entity.setSummary(result.getSummary());
        entity.setRawResponse(result.getRawResponse());
        entity.setModelUsed("deepseek-chat");
        aiAnalysisResultMapper.insert(entity);

        // 更新文章重要度评分
        article.setImportanceRating(result.getImportanceRating());
        article.setSummary(result.getSummary());
        articleMapper.updateById(article);

        // 发布内容分析完成事件（观察者模式）
        eventPublisher.publishEvent(new ContentEvent(this, article, entity));

        return buildVO(entity, article.getTitle());
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
        List<Article> articles = articleMapper.selectList(
                new LambdaQueryWrapper<Article>()
                        .orderByDesc(Article::getImportanceRating)
                        .orderByDesc(Article::getCreatedAt)
                        .last("LIMIT 10")
        );

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
