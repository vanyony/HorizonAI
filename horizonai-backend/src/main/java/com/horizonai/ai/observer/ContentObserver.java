package com.horizonai.ai.observer;

/**
 * 内容观察者接口 — 观察者模式
 */
public interface ContentObserver {

    /**
     * 当新的 AI 分析完成时触发
     */
    void onContentAnalyzed(ContentEvent event);
}
