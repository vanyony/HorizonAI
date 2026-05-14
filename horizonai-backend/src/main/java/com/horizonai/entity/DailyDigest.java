package com.horizonai.entity;

import com.baomidou.mybatisplus.annotation.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@TableName("daily_digests")
public class DailyDigest {

    @TableId(type = IdType.AUTO)
    private Long id;

    private LocalDate digestDate;

    private String title;

    private String summary;

    private String articleIds;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    // ========== 手动 getter/setter 方法（替代 Lombok @Data） ==========

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public LocalDate getDigestDate() { return digestDate; }
    public void setDigestDate(LocalDate digestDate) { this.digestDate = digestDate; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getSummary() { return summary; }
    public void setSummary(String summary) { this.summary = summary; }

    public String getArticleIds() { return articleIds; }
    public void setArticleIds(String articleIds) { this.articleIds = articleIds; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
