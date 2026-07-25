package com.horizonai.dto;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

public class ResearchAskRequest {
    private Long sessionId;
    @NotBlank(message = "问题不能为空")
    @Size(max = 1000, message = "问题不能超过1000个字符")
    private String question;

    public Long getSessionId() { return sessionId; }
    public void setSessionId(Long sessionId) { this.sessionId = sessionId; }
    public String getQuestion() { return question; }
    public void setQuestion(String question) { this.question = question; }
}
