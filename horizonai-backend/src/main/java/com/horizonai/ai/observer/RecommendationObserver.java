package com.horizonai.ai.observer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

/**
 * 推荐刷新观察者 — 当新内容分析完成后刷新推荐状态
 * Phase 4 实现基础版本：记录日志，后续可扩展复杂推荐逻辑
 */
@Component
public class RecommendationObserver implements ContentObserver {

    private static final Logger log = LoggerFactory.getLogger(RecommendationObserver.class);

    @Override
    @Async
    @EventListener
    public void onContentAnalyzed(ContentEvent event) {
        log.info("[观察者] RecommendationObserver 收到事件: articleId={}, importanceRating={}",
                event.getArticle().getId(), event.getAnalysisResult().getImportanceRating());

        // 推荐逻辑：
        // 高重要度（>=7）的文章会被首页今日速览优先展示
        // 这里预留扩展：可基于用户兴趣标签做个性化推送
        if (event.getAnalysisResult().getImportanceRating() >= 7) {
            log.info("[观察者] 高重要度文章，标记为优先推荐: {}", event.getArticle().getTitle());
        }
    }
}
