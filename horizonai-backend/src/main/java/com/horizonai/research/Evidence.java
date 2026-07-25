package com.horizonai.research;

public class Evidence {

    private Integer citationIndex;
    private String title;
    private String snippet;
    private String sourceUrl;
    private String sourceType;

    public Evidence() {}

    public Evidence(String title, String snippet, String sourceUrl, String sourceType) {
        this.title = title;
        this.snippet = snippet;
        this.sourceUrl = sourceUrl;
        this.sourceType = sourceType;
    }

    public Integer getCitationIndex() { return citationIndex; }
    public void setCitationIndex(Integer citationIndex) { this.citationIndex = citationIndex; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getSnippet() { return snippet; }
    public void setSnippet(String snippet) { this.snippet = snippet; }

    public String getSourceUrl() { return sourceUrl; }
    public void setSourceUrl(String sourceUrl) { this.sourceUrl = sourceUrl; }

    public String getSourceType() { return sourceType; }
    public void setSourceType(String sourceType) { this.sourceType = sourceType; }
}
