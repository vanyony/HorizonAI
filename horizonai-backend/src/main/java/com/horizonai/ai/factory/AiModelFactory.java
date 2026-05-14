package com.horizonai.ai.factory;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

/**
 * AI 模型工厂 — 根据配置创建对应的 AI 客户端
 */
@Slf4j
@Component
public class AiModelFactory {

    private final String modelType;
    private final String apiKey;
    private final String baseUrl;
    private final String model;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    public AiModelFactory(
            @Value("${ai.model.type}") String modelType,
            @Value("${ai.deepseek.api-key}") String apiKey,
            @Value("${ai.deepseek.base-url}") String baseUrl,
            @Value("${ai.deepseek.model}") String model,
            RestTemplate restTemplate,
            ObjectMapper objectMapper) {
        this.modelType = modelType;
        this.apiKey = apiKey;
        this.baseUrl = baseUrl;
        this.model = model;
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
    }

    /**
     * 根据配置返回对应的 AI 模型客户端
     * 扩展新模型只需在此方法添加 case
     */
    public AiModelClient create() {
        switch (modelType.toLowerCase()) {
            case "deepseek":
                log.info("创建 DeepSeek 客户端: model={}", model);
                return new DeepSeekClient(apiKey, baseUrl, model, restTemplate, objectMapper);
            case "qwen":
                // 预留 Qwen 扩展
                throw new UnsupportedOperationException("Qwen 模型暂未实现");
            case "openai":
                // 预留 OpenAI 扩展
                throw new UnsupportedOperationException("OpenAI 模型暂未实现");
            default:
                throw new IllegalArgumentException("不支持的 AI 模型类型: " + modelType);
        }
    }
}
