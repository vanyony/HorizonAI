package com.horizonai.service.impl;

import com.horizonai.ai.strategy.AnalysisResult;
import com.horizonai.entity.AiAnalysisResult;
import com.horizonai.entity.Article;
import com.horizonai.mapper.AiAnalysisResultMapper;
import com.horizonai.mapper.ArticleMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AiAnalysisPersistenceServiceTest {

    private ArticleMapper articleMapper;
    private AiAnalysisResultMapper analysisResultMapper;
    private AiAnalysisPersistenceService service;

    @BeforeEach
    void setUp() {
        articleMapper = mock(ArticleMapper.class);
        analysisResultMapper = mock(AiAnalysisResultMapper.class);
        service = new AiAnalysisPersistenceService(articleMapper, analysisResultMapper);
    }

    @Test
    void missingContentHashCannotReuseExistingAnalysis() {
        assertNull(service.findExisting(1L, null, "model-a", "prompt-v1"));

        verify(analysisResultMapper, never()).selectOne(any());
    }

    @Test
    void savesVersionedAnalysisAndUpdatesArticleStatus() {
        Article article = new Article();
        article.setId(7L);
        article.setContentHash("hash-7");

        AnalysisResult result = new AnalysisResult();
        result.setImportanceRating(5);
        result.setTargetAudience("backend developers");
        result.setIndustryImpact("high");
        result.setLearningSuggestions("read the paper");
        result.setSummary("summary");
        result.setRawResponse("raw");

        doAnswer(invocation -> {
            AiAnalysisResult saved = invocation.getArgument(0);
            saved.setId(100L);
            return 1;
        }).when(analysisResultMapper).insert(any(AiAnalysisResult.class));

        AiAnalysisResult saved = service.saveSuccess(article, result, "model-a", "prompt-v2");

        assertEquals(100L, saved.getId());
        assertEquals(7L, saved.getArticleId());
        assertEquals(5, saved.getImportanceRating());
        assertEquals("backend developers", saved.getTargetAudience());
        assertEquals("high", saved.getIndustryImpact());
        assertEquals("read the paper", saved.getLearningSuggestions());
        assertEquals("summary", saved.getSummary());
        assertEquals("raw", saved.getRawResponse());
        assertEquals("model-a", saved.getModelUsed());
        assertEquals("prompt-v2", saved.getPromptVersion());
        assertEquals("hash-7", saved.getContentHash());

        assertEquals(5, article.getImportanceRating());
        assertEquals("summary", article.getSummary());
        assertEquals("SUCCEEDED", article.getAnalysisStatus());
        verify(articleMapper).updateById(article);
    }

    @Test
    void markStatusDoesNothingForMissingArticle() {
        when(articleMapper.selectById(999L)).thenReturn(null);

        service.markStatus(999L, "RUNNING");

        verify(articleMapper, never()).updateById(any(Article.class));
    }
}
