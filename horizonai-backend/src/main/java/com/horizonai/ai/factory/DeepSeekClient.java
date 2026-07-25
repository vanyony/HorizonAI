package com.horizonai.ai.factory;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;

import java.util.*;

/**
 * DeepSeek API 客户端（OpenAI 兼容接口）
 */
public class DeepSeekClient implements AiModelClient {

    private static final Logger log = LoggerFactory.getLogger(DeepSeekClient.class);

    private final String apiKey;
    private final String baseUrl;
    private final String model;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    public DeepSeekClient(String apiKey, String baseUrl, String model,
                          RestTemplate restTemplate, ObjectMapper objectMapper) {
        this.apiKey = apiKey;
        this.baseUrl = baseUrl;
        this.model = model;
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
    }

    @Override
    public String chat(String systemPrompt, String userMessage) {
        return chatWithHistory(systemPrompt,
                Collections.singletonList(new AiMessage("user", userMessage)));
    }

    @Override
    public String chatWithHistory(String systemPrompt, List<AiMessage> messages) {
        long startedAt = System.nanoTime();
        try {
            List<Map<String, String>> messageList = new ArrayList<>();

            // System Prompt
            Map<String, String> systemMsg = new HashMap<>();
            systemMsg.put("role", "system");
            systemMsg.put("content", systemPrompt);
            messageList.add(systemMsg);

            // 历史 + 当前消息
            for (AiMessage msg : messages) {
                Map<String, String> m = new HashMap<>();
                m.put("role", msg.getRole());
                m.put("content", msg.getContent());
                messageList.add(m);
            }

            Map<String, Object> body = new HashMap<>();
            body.put("model", model);
            body.put("messages", messageList);
            body.put("temperature", 0.7);
            body.put("max_tokens", 2048);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(apiKey);

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);

            ResponseEntity<String> response = restTemplate.postForEntity(
                    baseUrl + "/chat/completions", request, String.class);

            JsonNode root = objectMapper.readTree(response.getBody());
            String content = root.path("choices").get(0)
                    .path("message").path("content").asText();
            log.info("外部模型调用完成: service=deepseek, model={}, status={}, durationMs={}",
                    model,
                    response.getStatusCodeValue(),
                    (System.nanoTime() - startedAt) / 1_000_000);
            return content;
        } catch (Exception e) {
            log.error("外部模型调用失败: service=deepseek, model={}, durationMs={}",
                    model,
                    (System.nanoTime() - startedAt) / 1_000_000,
                    e);
            throw new RuntimeException("AI 服务暂时不可用: " + e.getMessage());
        }
    }
}
