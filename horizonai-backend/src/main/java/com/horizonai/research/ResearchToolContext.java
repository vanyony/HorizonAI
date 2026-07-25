package com.horizonai.research;

public class ResearchToolContext {

    private final Long userId;
    private final String query;
    private final int maxResults;

    public ResearchToolContext(Long userId, String query, int maxResults) {
        this.userId = userId;
        this.query = query;
        this.maxResults = maxResults;
    }

    public Long getUserId() { return userId; }
    public String getQuery() { return query; }
    public int getMaxResults() { return maxResults; }
}
