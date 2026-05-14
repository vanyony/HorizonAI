package com.horizonai.entity;

import com.baomidou.mybatisplus.annotation.*;
import java.time.LocalDateTime;

@TableName("ai_analysis_results")
public class AiAnalysisResult {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long articleId;

    private Integer importanceRating;

    private String targetAudience;

    private String industryImpact;

    private String learningSuggestions;

    private String summary;

    private String rawResponse;

    private String modelUsed;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    // ========== 手动 getter/setter 方法（替代 Lombok @Data） ==========

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getArticleId() { return articleId; }
    public void setArticleId(Long articleId) { this.articleId = articleId; }

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

    public String getRawResponse() { return rawResponse; }
    public void setRawResponse(String rawResponse) { this.rawResponse = rawResponse; }

    public String getModelUsed() { return modelUsed; }
    public void setModelUsed(String modelUsed) { this.modelUsed = modelUsed; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
