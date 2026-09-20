package com.horizonai.service;

import com.horizonai.collector.CollectedContent;
import com.horizonai.entity.Article;
import com.horizonai.mapper.ArticleMapper;
import com.horizonai.mapper.ArticleTagMapper;
import com.horizonai.mapper.TagMapper;
import com.horizonai.util.ContentFingerprint;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.dao.DuplicateKeyException;
import com.horizonai.task.PipelineTaskSubmissionService;
import com.horizonai.task.PipelineTaskType;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ContentIngestionServiceTest {

    private ArticleMapper articleMapper;
    private ArticleTagMapper articleTagMapper;
    private TagMapper tagMapper;
    private PipelineTaskSubmissionService taskSubmissionService;
    private ContentIngestionService service;

    @BeforeEach
    void setUp() {
        articleMapper = mock(ArticleMapper.class);
        articleTagMapper = mock(ArticleTagMapper.class);
        tagMapper = mock(TagMapper.class);
        taskSubmissionService = mock(PipelineTaskSubmissionService.class);
        service = new ContentIngestionService(
                articleMapper,
                articleTagMapper,
                tagMapper,
                new ContentFingerprint(),
                taskSubmissionService,
                "v1"
        );
    }

    @Test
    void rejectsContentWithoutRequiredIdentityFields() {
        CollectedContent content = new CollectedContent();

        assertThrows(IllegalArgumentException.class, () -> service.ingest(content));
    }

    @Test
    void sourceIdentityDuplicateIsReturnedBeforeContentFingerprintLookup() {
        Article existing = article(11L, "github", "issue-1", "old-hash");
        when(articleMapper.selectOne(any())).thenReturn(existing);

        IngestionResult result = service.ingest(content("github", "issue-1", "New title"));

        assertFalse(result.isCreated());
        assertEquals(11L, result.getArticleId());
        assertEquals("SOURCE_IDENTITY_DUPLICATE", result.getReason());
    }

    @Test
    void contentHashDuplicateIsReturnedAfterSourceIdentityMiss() {
        Article existing = article(12L, "rss", "entry-1", "same-hash");
        when(articleMapper.selectOne(any())).thenReturn(null, existing);

        CollectedContent content = content("rss", "entry-2", "Same title");
        content.setContent("Same body");
        IngestionResult result = service.ingest(content);

        assertFalse(result.isCreated());
        assertEquals(12L, result.getArticleId());
        assertEquals("CONTENT_DUPLICATE", result.getReason());
    }

    @Test
    void createsArticleAndAtomicallySubmitsAnalysisTask() {
        when(articleMapper.selectOne(any())).thenReturn(null, null);
        doAnswer(invocation -> {
            Article inserted = invocation.getArgument(0);
            inserted.setId(77L);
            return 1;
        }).when(articleMapper).insert(any(Article.class));

        CollectedContent content = content("demo", "demo-1", "Demo title");
        content.setSummary("Demo summary");
        content.setContent("Demo body");

        IngestionResult result = service.ingest(content);

        assertTrue(result.isCreated());
        assertEquals(77L, result.getArticleId());
        assertEquals("CREATED", result.getReason());

        ArgumentCaptor<Article> articleCaptor = ArgumentCaptor.forClass(Article.class);
        verify(articleMapper).insert(articleCaptor.capture());
        Article inserted = articleCaptor.getValue();
        assertEquals("demo", inserted.getSourceName());
        assertEquals("demo-1", inserted.getExternalId());
        assertEquals("Demo title", inserted.getTitle());
        assertNotNull(inserted.getContentHash());
        assertEquals("PENDING", inserted.getAnalysisStatus());

        verify(taskSubmissionService).submit(
                org.mockito.ArgumentMatchers.eq(PipelineTaskType.ANALYZE_ARTICLE),
                org.mockito.ArgumentMatchers.eq("77"),
                org.mockito.ArgumentMatchers.eq("article-analysis:77:" + inserted.getContentHash() + ":v1"),
                org.mockito.ArgumentMatchers.eq("{}")
        );
    }

    @Test
    void duplicateInsertReturnsConcurrentArticleWhenUniqueConstraintWinsRace() {
        Article concurrent = article(88L, "github", "issue-88", "hash-88");
        when(articleMapper.selectOne(any())).thenReturn(null, null, concurrent);
        DuplicateKeyException duplicate = new DuplicateKeyException("duplicate article");
        when(articleMapper.insert(any(Article.class))).thenThrow(duplicate);

        IngestionResult result = service.ingest(content("github", "issue-88", "Title"));

        assertFalse(result.isCreated());
        assertEquals(88L, result.getArticleId());
        assertEquals("CONCURRENT_DUPLICATE", result.getReason());
    }

    private CollectedContent content(String sourceName, String externalId, String title) {
        CollectedContent content = new CollectedContent();
        content.setSourceName(sourceName);
        content.setExternalId(externalId);
        content.setSourceType(sourceName);
        content.setTitle(title);
        content.setSourceUrl("https://example.test/" + externalId);
        return content;
    }

    private Article article(Long id, String sourceName, String externalId, String hash) {
        Article article = new Article();
        article.setId(id);
        article.setSourceName(sourceName);
        article.setExternalId(externalId);
        article.setContentHash(hash);
        return article;
    }
}
