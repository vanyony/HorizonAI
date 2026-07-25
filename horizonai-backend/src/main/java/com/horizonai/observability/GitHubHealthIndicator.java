package com.horizonai.observability;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Component("github")
public class GitHubHealthIndicator implements HealthIndicator {

    private final RestTemplate restTemplate;
    private final String githubToken;

    public GitHubHealthIndicator(
            RestTemplate restTemplate,
            @Value("${github.api-token:}") String githubToken) {
        this.restTemplate = restTemplate;
        this.githubToken = githubToken;
    }

    @Override
    public Health health() {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("User-Agent", "HorizonAI-Health");
            if (StringUtils.hasText(githubToken)
                    && !"your_github_token_here".equals(githubToken)) {
                headers.setBearerAuth(githubToken);
            }
            ResponseEntity<Map> response = restTemplate.exchange(
                    "https://api.github.com/rate_limit",
                    HttpMethod.GET,
                    new HttpEntity<>(headers),
                    Map.class
            );
            return Health.up()
                    .withDetail("statusCode", response.getStatusCodeValue())
                    .withDetail("authenticated", headers.containsKey(HttpHeaders.AUTHORIZATION))
                    .build();
        } catch (Exception e) {
            return Health.down()
                    .withDetail("reason", e.getClass().getSimpleName())
                    .build();
        }
    }
}
