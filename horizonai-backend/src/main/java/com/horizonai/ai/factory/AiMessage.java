package com.horizonai.ai.factory;

public class AiMessage {
    private String role;
    private String content;

    // ========== 手动全参构造器（替代 Lombok @AllArgsConstructor） ==========

    public AiMessage(String role, String content) {
        this.role = role;
        this.content = content;
    }

    // ========== 手动 getter/setter 方法（替代 Lombok @Data） ==========

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
}
