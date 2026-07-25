package com.horizonai.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.horizonai.common.BusinessException;
import com.horizonai.dto.ArticleSaveDTO;
import com.horizonai.entity.Article;
import com.horizonai.entity.ArticleTag;
import com.horizonai.entity.Tag;
import com.horizonai.mapper.ArticleMapper;
import com.horizonai.mapper.ArticleTagMapper;
import com.horizonai.mapper.TagMapper;
import com.horizonai.service.ArticleService;
import com.horizonai.vo.ArticleVO;
import com.horizonai.vo.TagVO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ArticleServiceImpl implements ArticleService {

    private final ArticleMapper articleMapper;
    private final ArticleTagMapper articleTagMapper;
    private final TagMapper tagMapper;

    // ========== 手动构造器（替代 Lombok @RequiredArgsConstructor） ==========

    public ArticleServiceImpl(ArticleMapper articleMapper,
                              ArticleTagMapper articleTagMapper,
                              TagMapper tagMapper) {
        this.articleMapper = articleMapper;
        this.articleTagMapper = articleTagMapper;
        this.tagMapper = tagMapper;
    }

    @Override
    public Page<ArticleVO> page(Integer pageNum, Integer pageSize, String sourceType, Long tagId) {
        Page<Article> page = new Page<>(pageNum, pageSize);

        // 按标签筛选时需要子查询文章 ID
        if (tagId != null) {
            List<Long> articleIds = articleTagMapper.selectList(
                    new LambdaQueryWrapper<ArticleTag>()
                            .eq(ArticleTag::getTagId, tagId)
            ).stream().map(ArticleTag::getArticleId).collect(Collectors.toList());

            if (articleIds.isEmpty()) {
                return new Page<>();
            }

            LambdaQueryWrapper<Article> wrapper = new LambdaQueryWrapper<>();
            wrapper.in(Article::getId, articleIds);
            if (StringUtils.hasText(sourceType)) {
                wrapper.eq(Article::getSourceType, sourceType);
            }
            wrapper.orderByDesc(Article::getCreatedAt);
            articleMapper.selectPage(page, wrapper);
        } else {
            LambdaQueryWrapper<Article> wrapper = new LambdaQueryWrapper<>();
            if (StringUtils.hasText(sourceType)) {
                wrapper.eq(Article::getSourceType, sourceType);
            }
            wrapper.orderByDesc(Article::getCreatedAt);
            articleMapper.selectPage(page, wrapper);
        }

        // 转换为 VO，填充标签
        Page<ArticleVO> voPage = new Page<>(pageNum, pageSize, page.getTotal());
        voPage.setRecords(toArticleVOList(page.getRecords()));
        return voPage;
    }

    @Override
    public ArticleVO getById(Long id) {
        Article article = articleMapper.selectById(id);
        if (article == null) {
            throw new BusinessException("文章不存在");
        }
        return toArticleVO(article);
    }

    @Override
    @Transactional
    public ArticleVO create(ArticleSaveDTO dto) {
        Article article = new Article();
        article.setTitle(dto.getTitle());
        article.setSummary(dto.getSummary());
        article.setContent(dto.getContent());
        article.setSourceUrl(dto.getSourceUrl());
        article.setSourceType(dto.getSourceType());
        article.setPublishDate(dto.getPublishDate());
        articleMapper.insert(article);

        // 保存标签关联
        saveTags(article.getId(), dto.getTagIds());

        return toArticleVO(article);
    }

    @Override
    @Transactional
    public ArticleVO update(Long id, ArticleSaveDTO dto) {
        Article article = articleMapper.selectById(id);
        if (article == null) {
            throw new BusinessException("文章不存在");
        }

        article.setTitle(dto.getTitle());
        article.setSummary(dto.getSummary());
        article.setContent(dto.getContent());
        article.setSourceUrl(dto.getSourceUrl());
        article.setSourceType(dto.getSourceType());
        article.setPublishDate(dto.getPublishDate());
        articleMapper.updateById(article);

        // 更新标签关联：先删后插
        articleTagMapper.delete(new LambdaQueryWrapper<ArticleTag>()
                .eq(ArticleTag::getArticleId, id));
        saveTags(id, dto.getTagIds());

        return toArticleVO(article);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        // 删除标签关联
        articleTagMapper.delete(new LambdaQueryWrapper<ArticleTag>()
                .eq(ArticleTag::getArticleId, id));
        articleMapper.deleteById(id);
    }

    @Override
    public boolean existsBySourceUrl(String sourceUrl) {
        if (!StringUtils.hasText(sourceUrl)) {
            return false;
        }
        return articleMapper.selectCount(
                new LambdaQueryWrapper<Article>()
                        .eq(Article::getSourceUrl, sourceUrl)
        ) > 0;
    }

    private void saveTags(Long articleId, List<Long> tagIds) {
        if (tagIds != null && !tagIds.isEmpty()) {
            for (Long tagId : tagIds) {
                ArticleTag at = new ArticleTag();
                at.setArticleId(articleId);
                at.setTagId(tagId);
                articleTagMapper.insert(at);
            }
        }
    }

    private List<ArticleVO> toArticleVOList(List<Article> articles) {
        if (articles.isEmpty()) return new ArrayList<>();

        List<Long> articleIds = articles.stream().map(Article::getId).collect(Collectors.toList());

        // 批量查询标签关联
        List<ArticleTag> relations = articleTagMapper.selectList(
                new LambdaQueryWrapper<ArticleTag>().in(ArticleTag::getArticleId, articleIds)
        );

        // 批量查询标签
        List<Long> tagIds = relations.stream().map(ArticleTag::getTagId).distinct().collect(Collectors.toList());
        Map<Long, Tag> tagMap = tagMapper.selectBatchIds(tagIds).stream()
                .collect(Collectors.toMap(Tag::getId, t -> t));

        // 组装
        Map<Long, List<TagVO>> articleTagsMap = relations.stream()
                .collect(Collectors.groupingBy(
                        ArticleTag::getArticleId,
                        Collectors.mapping(r -> {
                            Tag t = tagMap.get(r.getTagId());
                            return t != null ? new TagVO(t.getId(), t.getName(), t.getDescription(), t.getCreatedAt()) : null;
                        }, Collectors.toList())
                ));

        return articles.stream().map(a -> {
            List<TagVO> tags = articleTagsMap.getOrDefault(a.getId(), new ArrayList<>());
            return new ArticleVO(a.getId(), a.getTitle(), a.getSummary(), a.getContent(),
                    a.getSourceUrl(), a.getSourceType(), a.getPublishDate(),
                    a.getImportanceRating(), a.getCreatedAt(), tags);
        }).collect(Collectors.toList());
    }

    private ArticleVO toArticleVO(Article a) {
        return toArticleVOList(List.of(a)).get(0);
    }
}
