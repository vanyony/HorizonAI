package com.horizonai.ai.factory;

import java.util.List;

/**
 * AI 模型客户端 — 工厂模式的产品接口
 */
public interface AiModelClient {

    /**
     * 单轮对话
     */
    String chat(String systemPrompt, String userMessage);

    /**
     * 多轮对话（携带历史记录）
     */
    String chatWithHistory(String systemPrompt, List<AiMessage> messages);
}
