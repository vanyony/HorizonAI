package com.horizonai.dto;

import javax.validation.constraints.NotBlank;

public class ChatSendRequest {

    @NotBlank(message = "消息内容不能为空")
    private String content;

    // ========== 手动 getter/setter 方法（替代 Lombok @Data） ==========

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
}
