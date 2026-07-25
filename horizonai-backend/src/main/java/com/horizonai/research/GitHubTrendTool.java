package com.horizonai.research;

import com.horizonai.util.GitHubApiClient;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class GitHubTrendTool implements ResearchTool {

    private final GitHubApiClient gitHubApiClient;

    public GitHubTrendTool(GitHubApiClient gitHubApiClient) {
        this.gitHubApiClient = gitHubApiClient;
    }

    @Override
    public String name() {
        return "github_trend";
    }

    @Override
    public List<Evidence> execute(ResearchToolContext context) {
        return gitHubApiClient.fetchTrendingRepos().stream()
                .limit(context.getMaxResults())
                .map(this::toEvidence)
                .collect(Collectors.toList());
    }

    private Evidence toEvidence(Map<String, Object> repository) {
        String name = String.valueOf(repository.getOrDefault("full_name", "GitHub Repository"));
        String description = String.valueOf(repository.getOrDefault("description", ""));
        String language = String.valueOf(repository.getOrDefault("language", "Unknown"));
        String stars = String.valueOf(repository.getOrDefault("stargazers_count", "Unknown"));
        String snippet = String.format("%s；主要语言：%s；Stars：%s",
                description, language, stars);
        return new Evidence(
                name,
                snippet,
                String.valueOf(repository.getOrDefault("html_url", "")),
                "GITHUB"
        );
    }
}
