package com.horizonai.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.horizonai.ai.strategy.AnalysisResult;
import com.horizonai.entity.AiAnalysisResult;
import com.horizonai.entity.Article;
import com.horizonai.mapper.AiAnalysisResultMapper;
import com.horizonai.mapper.ArticleMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AiAnalysisPersistenceService {

    private final ArticleMapper articleMapper;
    private final AiAnalysisResultMapper analysisResultMapper;

    public AiAnalysisPersistenceService(ArticleMapper articleMapper,
                                        AiAnalysisResultMapper analysisResultMapper) {
        this.articleMapper = articleMapper;
        this.analysisResultMapper = analysisResultMapper;
    }

    public AiAnalysisResult findExisting(Long articleId,
                                         String contentHash,
                                         String model,
                                         String promptVersion) {
        if (contentHash == null) {
            return null;
        }
        return analysisResultMapper.selectOne(
                new LambdaQueryWrapper<AiAnalysisResult>()
                        .eq(AiAnalysisResult::getArticleId, articleId)
                        .eq(AiAnalysisResult::getContentHash, contentHash)
                        .eq(AiAnalysisResult::getModelUsed, model)
                        .eq(AiAnalysisResult::getPromptVersion, promptVersion)
                        .last("LIMIT 1")
        );
    }

    @Transactional
    public void markStatus(Long articleId, String status) {
        Article article = articleMapper.selectById(articleId);
        if (article != null) {
            article.setAnalysisStatus(status);
            articleMapper.updateById(article);
        }
    }

    @Transactional
    public AiAnalysisResult saveSuccess(Article article,
                                        AnalysisResult result,
                                        String model,
                                        String promptVersion) {
        AiAnalysisResult entity = new AiAnalysisResult();
        entity.setArticleId(article.getId());
        entity.setImportanceRating(result.getImportanceRating());
        entity.setTargetAudience(result.getTargetAudience());
        entity.setIndustryImpact(result.getIndustryImpact());
        entity.setLearningSuggestions(result.getLearningSuggestions());
        entity.setSummary(result.getSummary());
        entity.setRawResponse(result.getRawResponse());
        entity.setModelUsed(model);
        entity.setPromptVersion(promptVersion);
        entity.setContentHash(article.getContentHash());
        analysisResultMapper.insert(entity);

        article.setImportanceRating(result.getImportanceRating());
        article.setSummary(result.getSummary());
        article.setAnalysisStatus("SUCCEEDED");
        articleMapper.updateById(article);
        return entity;
    }
}
