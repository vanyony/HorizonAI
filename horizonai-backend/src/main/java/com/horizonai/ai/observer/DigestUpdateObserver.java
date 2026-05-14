package com.horizonai.ai.observer;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.horizonai.entity.Article;
import com.horizonai.entity.DailyDigest;
import com.horizonai.mapper.ArticleMapper;
import com.horizonai.mapper.DailyDigestMapper;
import com.horizonai.ai.factory.AiModelClient;
import com.horizonai.ai.factory.AiModelFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 每日摘要更新观察者 — 当新内容分析完成后，判断是否需要更新今日摘要
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DigestUpdateObserver implements ContentObserver {

    private final DailyDigestMapper dailyDigestMapper;
    private final ArticleMapper articleMapper;
    private final AiModelFactory aiModelFactory;
    private final ObjectMapper objectMapper;

    @Override
    @Async
    @EventListener
    public void onContentAnalyzed(ContentEvent event) {
        log.info("[观察者] DigestUpdateObserver 收到事件: articleId={}", event.getArticle().getId());

        LocalDate today = LocalDate.now();
        Long count = dailyDigestMapper.selectCount(
                new LambdaQueryWrapper<DailyDigest>().eq(DailyDigest::getDigestDate, today));

        // 如果今天已有摘要则跳过
        if (count > 0) {
            log.info("[观察者] 今日摘要已存在，跳过生成");
            return;
        }

        // 获取今日重要度最高的文章
        List<Article> topArticles = articleMapper.selectList(
                new LambdaQueryWrapper<Article>()
                        .ge(Article::getImportanceRating, 5)
                        .orderByDesc(Article::getImportanceRating)
                        .last("LIMIT 8"));

        if (topArticles.size() < 3) {
            log.info("[观察者] 高重要度文章不足3篇，跳过摘要生成");
            return;
        }

        // 调用 AI 生成每日摘要
        try {
            AiModelClient client = aiModelFactory.create();
            String articleList = topArticles.stream()
                    .map(a -> String.format("- [%s] %s (重要度:%d)", a.getSourceType(), a.getTitle(), a.getImportanceRating()))
                    .collect(Collectors.joining("\n"));

            String systemPrompt = "你是观澜 AI 技术趋势分析师，请根据今日技术热点列表生成一份简洁的每日技术摘要（200字以内）。"
                    + "摘要应包括：今日最重要的技术动态、值得关注的趋势、一句话行动建议。";
            String summary = client.chat(systemPrompt, articleList);

            DailyDigest digest = new DailyDigest();
            digest.setDigestDate(today);
            digest.setTitle("今日技术速览 - " + today);
            digest.setSummary(summary);
            digest.setArticleIds(objectMapper.writeValueAsString(
                    topArticles.stream().map(Article::getId).collect(Collectors.toList())));
            dailyDigestMapper.insert(digest);

            log.info("[观察者] 每日摘要已生成: date={}", today);
        } catch (Exception e) {
            log.error("[观察者] 每日摘要生成失败", e);
        }
    }
}
