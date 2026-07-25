package com.horizonai.research;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.horizonai.entity.Article;
import com.horizonai.mapper.ArticleMapper;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class SiteSearchTool implements ResearchTool {

    private final ArticleMapper articleMapper;

    public SiteSearchTool(ArticleMapper articleMapper) {
        this.articleMapper = articleMapper;
    }

    @Override
    public String name() {
        return "site_search";
    }

    @Override
    public List<Evidence> execute(ResearchToolContext context) {
        String keyword = context.getQuery().trim();
        List<Article> articles = articleMapper.selectList(
                new LambdaQueryWrapper<Article>()
                        .and(wrapper -> wrapper
                                .like(Article::getTitle, keyword)
                                .or()
                                .like(Article::getSummary, keyword)
                                .or()
                                .like(Article::getContent, keyword))
                        .orderByDesc(Article::getImportanceRating)
                        .orderByDesc(Article::getCreatedAt)
                        .last("LIMIT " + context.getMaxResults())
        );
        return articles.stream()
                .map(article -> new Evidence(
                        article.getTitle(),
                        snippet(article),
                        article.getSourceUrl(),
                        "ARTICLE"
                ))
                .collect(Collectors.toList());
    }

    private String snippet(Article article) {
        String value = StringUtils.hasText(article.getSummary())
                ? article.getSummary()
                : article.getContent();
        if (!StringUtils.hasText(value)) {
            return "";
        }
        return value.length() > 300 ? value.substring(0, 300) : value;
    }
}
