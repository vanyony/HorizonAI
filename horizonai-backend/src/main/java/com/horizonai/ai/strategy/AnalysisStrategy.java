package com.horizonai.ai.strategy;

import com.horizonai.entity.Article;

/**
 * 分析策略接口 — 策略模式
 */
public interface AnalysisStrategy {

    /**
     * 构建分析用的 System Prompt（每种内容类型不同）
     */
    String buildSystemPrompt();

    /**
     * 构建分析用的 User Message
     */
    String buildUserMessage(Article article);

    /**
     * 获取策略标识
     */
    String getStrategyName();
}
