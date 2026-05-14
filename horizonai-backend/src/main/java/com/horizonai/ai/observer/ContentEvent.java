package com.horizonai.ai.observer;

import com.horizonai.entity.AiAnalysisResult;
import com.horizonai.entity.Article;
import org.springframework.context.ApplicationEvent;

/**
 * 内容分析完成事件 — 观察者模式中的事件对象
 */
public class ContentEvent extends ApplicationEvent {

    private final Article article;
    private final AiAnalysisResult analysisResult;

    public ContentEvent(Object source, Article article, AiAnalysisResult analysisResult) {
        super(source);
        this.article = article;
        this.analysisResult = analysisResult;
    }

    // ========== 手动 getter 方法（替代 Lombok @Getter） ==========

    public Article getArticle() { return article; }

    public AiAnalysisResult getAnalysisResult() { return analysisResult; }
}
