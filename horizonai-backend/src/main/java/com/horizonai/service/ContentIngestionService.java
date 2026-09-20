package com.horizonai.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.horizonai.collector.CollectedContent;
import com.horizonai.entity.Article;
import com.horizonai.entity.ArticleTag;
import com.horizonai.entity.Tag;
import com.horizonai.mapper.ArticleMapper;
import com.horizonai.mapper.ArticleTagMapper;
import com.horizonai.mapper.TagMapper;
import com.horizonai.util.ContentFingerprint;
import org.springframework.dao.DuplicateKeyException;
import com.horizonai.task.PipelineTaskSubmissionService;
import com.horizonai.task.PipelineTaskType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class ContentIngestionService {

    private final ArticleMapper articleMapper;
    private final ArticleTagMapper articleTagMapper;
    private final TagMapper tagMapper;
    private final ContentFingerprint contentFingerprint;
    private final PipelineTaskSubmissionService taskSubmissionService;
    private final String promptVersion;

    public ContentIngestionService(ArticleMapper articleMapper,
                                   ArticleTagMapper articleTagMapper,
                                   TagMapper tagMapper,
                                   ContentFingerprint contentFingerprint,
                                   PipelineTaskSubmissionService taskSubmissionService,
                                   @org.springframework.beans.factory.annotation.Value("${ai.analysis.prompt-version:v1}") String promptVersion) {
        this.articleMapper = articleMapper;
        this.articleTagMapper = articleTagMapper;
        this.tagMapper = tagMapper;
        this.contentFingerprint = contentFingerprint;
        this.taskSubmissionService = taskSubmissionService;
        this.promptVersion = promptVersion;
    }

    @Transactional
    public IngestionResult ingest(CollectedContent collected) {
        validate(collected);
        Article existing = findBySourceIdentity(collected.getSourceName(), collected.getExternalId());
        if (existing != null) {
            return new IngestionResult(existing.getId(), false, "SOURCE_IDENTITY_DUPLICATE");
        }

        String fingerprint = contentFingerprint.calculate(
                collected.getTitle(),
                StringUtils.hasText(collected.getContent())
                        ? collected.getContent()
                        : collected.getSummary()
        );
        existing = findByContentHash(fingerprint);
        if (existing != null) {
            return new IngestionResult(existing.getId(), false, "CONTENT_DUPLICATE");
        }

        Article article = new Article();
        article.setTitle(collected.getTitle());
        article.setSummary(collected.getSummary());
        article.setContent(collected.getContent());
        article.setSourceUrl(collected.getSourceUrl());
        article.setSourceType(collected.getSourceType());
        article.setSourceName(collected.getSourceName());
        article.setExternalId(collected.getExternalId());
        article.setContentHash(fingerprint);
        article.setCollectedAt(LocalDateTime.now());
        article.setAnalysisStatus("PENDING");
        article.setPublishDate(collected.getPublishDate() == null
                ? LocalDate.now()
                : collected.getPublishDate());

        try {
            articleMapper.insert(article);
        } catch (DuplicateKeyException e) {
            Article concurrent = findBySourceIdentity(
                    collected.getSourceName(),
                    collected.getExternalId()
            );
            if (concurrent == null) {
                concurrent = findByContentHash(fingerprint);
            }
            if (concurrent != null) {
                return new IngestionResult(concurrent.getId(), false, "CONCURRENT_DUPLICATE");
            }
            throw e;
        }

        saveMatchedTags(article.getId(), collected.getTags());
        taskSubmissionService.submit(
                PipelineTaskType.ANALYZE_ARTICLE,
                String.valueOf(article.getId()),
                String.format("article-analysis:%d:%s:%s", article.getId(), fingerprint, promptVersion),
                "{}"
        );
        return new IngestionResult(article.getId(), true, "CREATED");
    }

    private void validate(CollectedContent content) {
        if (!StringUtils.hasText(content.getSourceName())
                || !StringUtils.hasText(content.getExternalId())
                || !StringUtils.hasText(content.getTitle())
                || !StringUtils.hasText(content.getSourceType())) {
            throw new IllegalArgumentException("采集内容缺少来源、外部标识、标题或类型");
        }
    }

    private Article findBySourceIdentity(String sourceName, String externalId) {
        return articleMapper.selectOne(
                new LambdaQueryWrapper<Article>()
                        .eq(Article::getSourceName, sourceName)
                        .eq(Article::getExternalId, externalId)
                        .last("LIMIT 1")
        );
    }

    private Article findByContentHash(String contentHash) {
        return articleMapper.selectOne(
                new LambdaQueryWrapper<Article>()
                        .eq(Article::getContentHash, contentHash)
                        .last("LIMIT 1")
        );
    }

    private void saveMatchedTags(Long articleId, List<String> sourceTags) {
        if (sourceTags == null || sourceTags.isEmpty()) {
            return;
        }
        Set<String> normalizedTags = sourceTags.stream()
                .filter(StringUtils::hasText)
                .map(value -> value.toLowerCase(Locale.ROOT))
                .collect(Collectors.toSet());

        List<Tag> tags = tagMapper.selectList(null);
        for (Tag tag : tags) {
            String tagName = tag.getName().toLowerCase(Locale.ROOT);
            boolean matches = normalizedTags.stream()
                    .anyMatch(value -> value.contains(tagName) || tagName.contains(value));
            if (matches) {
                ArticleTag relation = new ArticleTag();
                relation.setArticleId(articleId);
                relation.setTagId(tag.getId());
                articleTagMapper.insert(relation);
            }
        }
    }
}
