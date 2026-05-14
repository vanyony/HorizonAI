package com.horizonai.vo;

import java.time.LocalDateTime;

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

    // ========== 无参构造器（替代 Lombok @NoArgsConstructor） ==========

    public AiAnalysisVO() {}

    // ========== 全参构造器（替代 Lombok @AllArgsConstructor） ==========

    public AiAnalysisVO(Long id, Long articleId, String articleTitle, Integer importanceRating,
                        String targetAudience, String industryImpact, String learningSuggestions,
                        String summary, String modelUsed, LocalDateTime createdAt) {
        this.id = id;
        this.articleId = articleId;
        this.articleTitle = articleTitle;
        this.importanceRating = importanceRating;
        this.targetAudience = targetAudience;
        this.industryImpact = industryImpact;
        this.learningSuggestions = learningSuggestions;
        this.summary = summary;
        this.modelUsed = modelUsed;
        this.createdAt = createdAt;
    }

    // ========== 手动 Builder 模式（替代 Lombok @Builder） ==========

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
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

        public Builder id(Long id) { this.id = id; return this; }
        public Builder articleId(Long articleId) { this.articleId = articleId; return this; }
        public Builder articleTitle(String articleTitle) { this.articleTitle = articleTitle; return this; }
        public Builder importanceRating(Integer importanceRating) { this.importanceRating = importanceRating; return this; }
        public Builder targetAudience(String targetAudience) { this.targetAudience = targetAudience; return this; }
        public Builder industryImpact(String industryImpact) { this.industryImpact = industryImpact; return this; }
        public Builder learningSuggestions(String learningSuggestions) { this.learningSuggestions = learningSuggestions; return this; }
        public Builder summary(String summary) { this.summary = summary; return this; }
        public Builder modelUsed(String modelUsed) { this.modelUsed = modelUsed; return this; }
        public Builder createdAt(LocalDateTime createdAt) { this.createdAt = createdAt; return this; }

        public AiAnalysisVO build() {
            return new AiAnalysisVO(id, articleId, articleTitle, importanceRating,
                    targetAudience, industryImpact, learningSuggestions,
                    summary, modelUsed, createdAt);
        }
    }

    // ========== 手动 getter/setter 方法（替代 Lombok @Data） ==========

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getArticleId() { return articleId; }
    public void setArticleId(Long articleId) { this.articleId = articleId; }

    public String getArticleTitle() { return articleTitle; }
    public void setArticleTitle(String articleTitle) { this.articleTitle = articleTitle; }

    public Integer getImportanceRating() { return importanceRating; }
    public void setImportanceRating(Integer importanceRating) { this.importanceRating = importanceRating; }

    public String getTargetAudience() { return targetAudience; }
    public void setTargetAudience(String targetAudience) { this.targetAudience = targetAudience; }

    public String getIndustryImpact() { return industryImpact; }
    public void setIndustryImpact(String industryImpact) { this.industryImpact = industryImpact; }

    public String getLearningSuggestions() { return learningSuggestions; }
    public void setLearningSuggestions(String learningSuggestions) { this.learningSuggestions = learningSuggestions; }

    public String getSummary() { return summary; }
    public void setSummary(String summary) { this.summary = summary; }

    public String getModelUsed() { return modelUsed; }
    public void setModelUsed(String modelUsed) { this.modelUsed = modelUsed; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
