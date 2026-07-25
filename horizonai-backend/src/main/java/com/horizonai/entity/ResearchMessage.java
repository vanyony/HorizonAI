package com.horizonai.entity;

import com.baomidou.mybatisplus.annotation.*;
import java.time.LocalDateTime;

@TableName("research_messages")
public class ResearchMessage {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long sessionId;
    private String role;
    private String content;
    private String citationsJson;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getSessionId() { return sessionId; }
    public void setSessionId(Long sessionId) { this.sessionId = sessionId; }
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public String getCitationsJson() { return citationsJson; }
    public void setCitationsJson(String citationsJson) { this.citationsJson = citationsJson; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
