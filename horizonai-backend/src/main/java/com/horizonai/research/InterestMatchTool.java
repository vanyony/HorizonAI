package com.horizonai.research;

import com.horizonai.service.RecommendationService;
import com.horizonai.vo.ArticleVO;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class InterestMatchTool implements ResearchTool {

    private final RecommendationService recommendationService;

    public InterestMatchTool(RecommendationService recommendationService) {
        this.recommendationService = recommendationService;
    }

    @Override
    public String name() {
        return "interest_match";
    }

    @Override
    public List<Evidence> execute(ResearchToolContext context) {
        return recommendationService.recommend(
                        context.getUserId(),
                        context.getMaxResults()
                ).stream()
                .map(this::toEvidence)
                .collect(Collectors.toList());
    }

    private Evidence toEvidence(ArticleVO article) {
        return new Evidence(
                article.getTitle(),
                article.getSummary(),
                article.getSourceUrl(),
                "INTEREST_MATCH"
        );
    }
}
