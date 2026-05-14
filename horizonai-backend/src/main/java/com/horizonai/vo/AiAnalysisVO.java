package com.horizonai.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AiAnalysisVO {

    private Long id;
    private Long articleId;
    private String articleTitle;
    private Integer importanceRating;
    private String targetAudience;
    private String industryImpact;
    private String learningSuggestions;
    private String summary;
    private String modelUsed;
    private LocalDateTime createdAt;
}
