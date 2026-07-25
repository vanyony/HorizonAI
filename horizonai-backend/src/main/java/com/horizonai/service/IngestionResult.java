package com.horizonai.service;

public class IngestionResult {

    private final Long articleId;
    private final boolean created;
    private final String reason;

    public IngestionResult(Long articleId, boolean created, String reason) {
        this.articleId = articleId;
        this.created = created;
        this.reason = reason;
    }

    public Long getArticleId() { return articleId; }
    public boolean isCreated() { return created; }
    public String getReason() { return reason; }
}
