package com.horizonai.vo;

import java.time.LocalDateTime;

public class BrowseHistoryVO {

    private Long articleId;
    private String articleTitle;
    private String sourceType;
    private LocalDateTime browseTime;

    // ========== 手动全参构造器（替代 Lombok @AllArgsConstructor） ==========

    public BrowseHistoryVO(Long articleId, String articleTitle, String sourceType, LocalDateTime browseTime) {
        this.articleId = articleId;
        this.articleTitle = articleTitle;
        this.sourceType = sourceType;
        this.browseTime = browseTime;
    }

    // ========== 手动 getter/setter 方法（替代 Lombok @Data） ==========

    public Long getArticleId() { return articleId; }
    public void setArticleId(Long articleId) { this.articleId = articleId; }

    public String getArticleTitle() { return articleTitle; }
    public void setArticleTitle(String articleTitle) { this.articleTitle = articleTitle; }

    public String getSourceType() { return sourceType; }
    public void setSourceType(String sourceType) { this.sourceType = sourceType; }

    public LocalDateTime getBrowseTime() { return browseTime; }
    public void setBrowseTime(LocalDateTime browseTime) { this.browseTime = browseTime; }
}
