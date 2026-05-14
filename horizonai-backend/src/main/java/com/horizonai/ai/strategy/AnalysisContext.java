package com.horizonai.ai.strategy;

import com.horizonai.ai.factory.AiModelClient;
import com.horizonai.ai.factory.AiModelFactory;
import com.horizonai.entity.Article;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * 分析上下文 — 策略模式上下文，根据文章类型选择对应策略
 */
@Slf4j
@Component
public class AnalysisContext {

    private final Map<String, AnalysisStrategy> strategyMap = new HashMap<>();
    private final AiModelFactory aiModelFactory;
    private final ObjectMapper objectMapper;

    public AnalysisContext(AiModelFactory aiModelFactory, ObjectMapper objectMapper) {
        this.aiModelFactory = aiModelFactory;
        this.objectMapper = objectMapper;

        // 注册策略
        strategyMap.put("NEWS", new NewsAnalysisStrategy());
        strategyMap.put("GITHUB", new GitHubAnalysisStrategy());
        strategyMap.put("TREND", new TrendAnalysisStrategy());
    }

    /**
     * 对文章执行 AI 分析
     */
    public AnalysisResult analyze(Article article) {
        AnalysisStrategy strategy = strategyMap.get(article.getSourceType());
        if (strategy == null) {
            throw new IllegalArgumentException("不支持的内容类型: " + article.getSourceType());
        }

        log.info("开始 AI 分析: articleId={}, type={}, strategy={}",
                article.getId(), article.getSourceType(), strategy.getStrategyName());

        AiModelClient client = aiModelFactory.create();
        String systemPrompt = strategy.buildSystemPrompt();
        String userMessage = strategy.buildUserMessage(article);

        String response = client.chat(systemPrompt, userMessage);
        log.info("AI 分析完成: articleId={}", article.getId());

        return parseResponse(response);
    }

    private AnalysisResult parseResponse(String rawResponse) {
        try {
            // 尝试从 AI 返回中提取 JSON
            String json = rawResponse;
            if (rawResponse.contains("```json")) {
                json = rawResponse.substring(
                        rawResponse.indexOf("```json") + 7,
                        rawResponse.lastIndexOf("```")
                );
            } else if (rawResponse.contains("```")) {
                json = rawResponse.substring(
                        rawResponse.indexOf("```") + 3,
                        rawResponse.lastIndexOf("```")
                );
            }
            json = json.trim();

            JsonNode root = objectMapper.readTree(json);

            AnalysisResult result = new AnalysisResult();
            result.setImportanceRating(root.path("importanceRating").asInt(0));
            result.setTargetAudience(root.path("targetAudience").asText(""));
            result.setIndustryImpact(root.path("industryImpact").asText(""));
            result.setLearningSuggestions(root.path("learningSuggestions").asText(""));
            result.setSummary(root.path("summary").asText(""));
            result.setRawResponse(rawResponse);
            return result;
        } catch (Exception e) {
            log.warn("AI 返回 JSON 解析失败，使用原始文本作为摘要: {}", e.getMessage());
            AnalysisResult result = new AnalysisResult();
            result.setImportanceRating(5);
            result.setSummary(rawResponse.length() > 200 ? rawResponse.substring(0, 200) : rawResponse);
            result.setRawResponse(rawResponse);
            return result;
        }
    }
}
