package com.horizonai.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * GitHub API 客户端 — 用于获取近期热门新项目
 */
@Component
public class GitHubApiClient {

    private static final Logger log = LoggerFactory.getLogger(GitHubApiClient.class);
    private final RestTemplate restTemplate;

    @org.springframework.beans.factory.annotation.Value("${github.api-token:}")
    private String githubToken;

    public GitHubApiClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    /**
     * 获取最近 7 天创建、按 Star 数排序的 GitHub 项目
     */
    public List<Map<String, Object>> fetchPopularRecentRepos() {
        long startedAt = System.nanoTime();
        String since = LocalDate.now().minusDays(7).toString();
        String url = "https://api.github.com/search/repositories?q=created:>" + since + "&sort=stars&order=desc";

        log.info("正在从 GitHub 拉取近期热门新项目: {}", url);

        try {
            HttpHeaders headers = new HttpHeaders();
            // GitHub API 必须包含 User-Agent
            headers.set("User-Agent", "HorizonAI-Agent");
            headers.set("Accept", "application/vnd.github.v3+json");

            // 如果配置了 Token，则添加认证头
            if (org.springframework.util.StringUtils.hasText(githubToken) && !githubToken.equals("your_github_token_here")) {
                headers.set("Authorization", "token " + githubToken);
                log.info("已启用 GitHub Token 进行认证请求");
            }

            HttpEntity<String> entity = new HttpEntity<>(headers);
            ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.GET, entity, Map.class);

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                List<Map<String, Object>> items = (List<Map<String, Object>>) response.getBody().get("items");
                if (items != null && !items.isEmpty()) {
                    log.info("成功从 GitHub 获取 {} 个项目", items.size());
                    log.info("外部 API 调用完成: service=github, status={}, durationMs={}",
                            response.getStatusCodeValue(),
                            (System.nanoTime() - startedAt) / 1_000_000);
                    return items;
                }
            }
        } catch (Exception e) {
            log.error("外部 API 调用失败: service=github, durationMs={}",
                    (System.nanoTime() - startedAt) / 1_000_000,
                    e);
            throw new IllegalStateException("GitHub 数据源暂时不可用", e);
        }

        return List.of();
    }
}
