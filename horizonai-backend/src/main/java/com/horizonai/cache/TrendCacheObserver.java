package com.horizonai.cache;

import com.horizonai.ai.observer.ContentEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
public class TrendCacheObserver {

    private final TrendCacheService trendCacheService;

    public TrendCacheObserver(TrendCacheService trendCacheService) {
        this.trendCacheService = trendCacheService;
    }

    @Async("pipelineTaskExecutor")
    @EventListener
    public void onContentAnalyzed(ContentEvent event) {
        trendCacheService.recordAnalyzed(event.getArticle());
    }
}
