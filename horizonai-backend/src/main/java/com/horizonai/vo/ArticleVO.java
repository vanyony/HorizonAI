package com.horizonai.vo;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public class ArticleVO {

    private Long id;
    private String title;
    private String summary;
    private String content;
    private String sourceUrl;
    private String sourceType;
    private LocalDate publishDate;
    private Integer importanceRating;
    private LocalDateTime createdAt;
    private List<TagVO> tags;

    // ========== 手动全参构造器（替代 Lombok @AllArgsConstructor） ==========

    public ArticleVO(Long id, String title, String summary, String content,
                     String sourceUrl, String sourceType, LocalDate publishDate,
                     Integer importanceRating, LocalDateTime createdAt, List<TagVO> tags) {
        this.id = id;
        this.title = title;
        this.summary = summary;
        this.content = content;
        this.sourceUrl = sourceUrl;
        this.sourceType = sourceType;
        this.publishDate = publishDate;
        this.importanceRating = importanceRating;
        this.createdAt = createdAt;
        this.tags = tags;
    }

    // ========== 手动 getter/setter 方法（替代 Lombok @Data） ==========

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getSummary() { return summary; }
    public void setSummary(String summary) { this.summary = summary; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public String getSourceUrl() { return sourceUrl; }
    public void setSourceUrl(String sourceUrl) { this.sourceUrl = sourceUrl; }

    public String getSourceType() { return sourceType; }
    public void setSourceType(String sourceType) { this.sourceType = sourceType; }

    public LocalDate getPublishDate() { return publishDate; }
    public void setPublishDate(LocalDate publishDate) { this.publishDate = publishDate; }

    public Integer getImportanceRating() { return importanceRating; }
    public void setImportanceRating(Integer importanceRating) { this.importanceRating = importanceRating; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public List<TagVO> getTags() { return tags; }
    public void setTags(List<TagVO> tags) { this.tags = tags; }
}
