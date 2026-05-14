package com.horizonai.vo;

import java.time.LocalDateTime;

public class ChatMessageVO {

    private Long id;
    private String role;
    private String content;
    private LocalDateTime createdAt;

    // ========== 无参构造器（替代 Lombok @NoArgsConstructor） ==========

    public ChatMessageVO() {}

    // ========== 全参构造器（替代 Lombok @AllArgsConstructor） ==========

    public ChatMessageVO(Long id, String role, String content, LocalDateTime createdAt) {
        this.id = id;
        this.role = role;
        this.content = content;
        this.createdAt = createdAt;
    }

    // ========== 手动 Builder 模式（替代 Lombok @Builder） ==========

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Long id;
        private String role;
        private String content;
        private LocalDateTime createdAt;

        public Builder id(Long id) { this.id = id; return this; }
        public Builder role(String role) { this.role = role; return this; }
        public Builder content(String content) { this.content = content; return this; }
        public Builder createdAt(LocalDateTime createdAt) { this.createdAt = createdAt; return this; }

        public ChatMessageVO build() {
            return new ChatMessageVO(id, role, content, createdAt);
        }
    }

    // ========== 手动 getter/setter 方法（替代 Lombok @Data） ==========

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
