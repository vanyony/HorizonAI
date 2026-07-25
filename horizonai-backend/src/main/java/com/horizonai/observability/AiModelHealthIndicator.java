package com.horizonai.observability;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component("aiModel")
public class AiModelHealthIndicator implements HealthIndicator {

    private final String modelType;
    private final String apiKey;
    private final String baseUrl;

    public AiModelHealthIndicator(
            @Value("${ai.model.type:deepseek}") String modelType,
            @Value("${ai.deepseek.api-key:}") String apiKey,
            @Value("${ai.deepseek.base-url:}") String baseUrl) {
        this.modelType = modelType;
        this.apiKey = apiKey;
        this.baseUrl = baseUrl;
    }

    @Override
    public Health health() {
        boolean configured = StringUtils.hasText(apiKey)
                && !apiKey.contains("your-deepseek-api-key");
        Health.Builder builder = configured ? Health.up() : Health.unknown();
        return builder
                .withDetail("modelType", modelType)
                .withDetail("baseUrl", baseUrl)
                .withDetail("credentialConfigured", configured)
                .withDetail("checkType", "configuration")
                .build();
    }
}
