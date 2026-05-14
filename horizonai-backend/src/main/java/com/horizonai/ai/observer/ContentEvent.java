package com.horizonai.ai.observer;

import com.horizonai.entity.AiAnalysisResult;
import com.horizonai.entity.Article;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

/**
 * 内容分析完成事件 — 观察者模式中的事件对象
 */
@Getter
public class ContentEvent extends ApplicationEvent {

    private final Article article;
    private final AiAnalysisResult analysisResult;

    public ContentEvent(Object source, Article article, AiAnalysisResult analysisResult) {
        super(source);
        this.article = article;
        this.analysisResult = analysisResult;
    }
}
