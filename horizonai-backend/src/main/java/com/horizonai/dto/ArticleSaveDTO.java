package com.horizonai.dto;

import javax.validation.constraints.NotBlank;
import java.time.LocalDate;
import java.util.List;

public class ArticleSaveDTO {

    @NotBlank(message = "标题不能为空")
    private String title;

    private String summary;

    private String content;

    private String sourceUrl;

    @NotBlank(message = "来源类型不能为空")
    private String sourceType;

    private LocalDate publishDate;

    private List<Long> tagIds;

    // ========== 手动 getter/setter 方法（替代 Lombok @Data） ==========

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

    public List<Long> getTagIds() { return tagIds; }
    public void setTagIds(List<Long> tagIds) { this.tagIds = tagIds; }
}
