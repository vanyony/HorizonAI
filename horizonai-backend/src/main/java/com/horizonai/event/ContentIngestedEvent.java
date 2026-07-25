package com.horizonai.event;

public class ContentIngestedEvent {

    private final Long articleId;
    private final String contentHash;

    public ContentIngestedEvent(Long articleId, String contentHash) {
        this.articleId = articleId;
        this.contentHash = contentHash;
    }

    public Long getArticleId() { return articleId; }
    public String getContentHash() { return contentHash; }
}
