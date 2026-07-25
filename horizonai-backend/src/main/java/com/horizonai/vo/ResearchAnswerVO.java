package com.horizonai.vo;

import com.horizonai.entity.ToolInvocation;
import com.horizonai.research.Evidence;
import java.util.List;

public class ResearchAnswerVO {
    private Long sessionId;
    private Long messageId;
    private String answer;
    private String traceId;
    private List<Evidence> evidences;
    private List<ToolInvocation> toolInvocations;

    public Long getSessionId() { return sessionId; }
    public void setSessionId(Long sessionId) { this.sessionId = sessionId; }
    public Long getMessageId() { return messageId; }
    public void setMessageId(Long messageId) { this.messageId = messageId; }
    public String getAnswer() { return answer; }
    public void setAnswer(String answer) { this.answer = answer; }
    public String getTraceId() { return traceId; }
    public void setTraceId(String traceId) { this.traceId = traceId; }
    public List<Evidence> getEvidences() { return evidences; }
    public void setEvidences(List<Evidence> evidences) { this.evidences = evidences; }
    public List<ToolInvocation> getToolInvocations() { return toolInvocations; }
    public void setToolInvocations(List<ToolInvocation> toolInvocations) {
        this.toolInvocations = toolInvocations;
    }
}
