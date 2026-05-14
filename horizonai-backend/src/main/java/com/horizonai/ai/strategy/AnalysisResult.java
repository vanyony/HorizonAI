package com.horizonai.ai.strategy;

/**
 * 分析结果 — AI 返回的解析后数据
 */
public class AnalysisResult {

    private Integer importanceRating;
    private String targetAudience;
    private String industryImpact;
    private String learningSuggestions;
    private String summary;
    private String rawResponse;

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
}
