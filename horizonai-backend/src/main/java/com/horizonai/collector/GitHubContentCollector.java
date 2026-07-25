package com.horizonai.collector;

import com.horizonai.util.GitHubApiClient;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Component
public class GitHubContentCollector implements ContentCollector {

    public static final String SOURCE_NAME = "GITHUB";

    private final GitHubApiClient gitHubApiClient;

    public GitHubContentCollector(GitHubApiClient gitHubApiClient) {
        this.gitHubApiClient = gitHubApiClient;
    }

    @Override
    public String sourceName() {
        return SOURCE_NAME;
    }

    @Override
    public List<CollectedContent> collect() {
        List<Map<String, Object>> repositories = gitHubApiClient.fetchTrendingRepos();
        List<CollectedContent> result = new ArrayList<>();

        for (Map<String, Object> repository : repositories) {
            String fullName = stringValue(repository.get("full_name"));
            String sourceUrl = stringValue(repository.get("html_url"));
            if (!StringUtils.hasText(fullName) || !StringUtils.hasText(sourceUrl)) {
                continue;
            }

            CollectedContent content = new CollectedContent();
            content.setSourceName(SOURCE_NAME);
            content.setExternalId(externalId(repository, fullName));
            content.setSourceType("GITHUB");
            content.setTitle(fullName);
            content.setSummary(stringValue(repository.get("description")));
            content.setContent(buildContent(repository));
            content.setSourceUrl(sourceUrl);
            content.setPublishDate(LocalDate.now());
            content.setTags(extractTags(repository));
            result.add(content);
        }
        return result;
    }

    private String externalId(Map<String, Object> repository, String fallback) {
        Object id = repository.get("id");
        return id == null ? fallback : String.valueOf(id);
    }

    private String buildContent(Map<String, Object> repository) {
        String description = stringValue(repository.get("description"));
        String language = stringValue(repository.get("language"));
        Object stars = repository.get("stargazers_count");
        return String.format("%s%n主要语言：%s%nStars：%s",
                description,
                StringUtils.hasText(language) ? language : "未知",
                stars == null ? "未知" : stars);
    }

    private List<String> extractTags(Map<String, Object> repository) {
        Set<String> tags = new LinkedHashSet<>();
        String language = stringValue(repository.get("language"));
        if (StringUtils.hasText(language)) {
            tags.add(language);
        }
        Object topicsValue = repository.get("topics");
        if (topicsValue instanceof List<?>) {
            for (Object topic : (List<?>) topicsValue) {
                String value = stringValue(topic);
                if (StringUtils.hasText(value)) {
                    tags.add(value);
                }
            }
        }
        return new ArrayList<>(tags);
    }

    private String stringValue(Object value) {
        return value == null ? "" : String.valueOf(value);
    }
}
