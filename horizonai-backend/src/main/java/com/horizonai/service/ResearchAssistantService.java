package com.horizonai.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.horizonai.ai.factory.AiModelClient;
import com.horizonai.ai.factory.AiModelFactory;
import com.horizonai.common.BusinessException;
import com.horizonai.entity.ResearchMessage;
import com.horizonai.entity.ResearchSession;
import com.horizonai.entity.ToolInvocation;
import com.horizonai.mapper.ResearchMessageMapper;
import com.horizonai.mapper.ResearchSessionMapper;
import com.horizonai.mapper.ToolInvocationMapper;
import com.horizonai.research.Evidence;
import com.horizonai.research.ResearchTool;
import com.horizonai.research.ResearchToolContext;
import com.horizonai.research.ResearchToolPlanner;
import com.horizonai.vo.ResearchAnswerVO;
import org.slf4j.MDC;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class ResearchAssistantService {

    private static final String SYSTEM_PROMPT =
            "你是 HorizonAI 技术研究助手。只能根据提供的证据回答问题；"
                    + "每个事实性结论必须使用 [1]、[2] 形式引用对应证据。"
                    + "如果证据不足，要明确说明，不得编造来源。回答使用中文。";

    private final ResearchSessionMapper sessionMapper;
    private final ResearchMessageMapper messageMapper;
    private final ToolInvocationMapper invocationMapper;
    private final ResearchToolPlanner toolPlanner;
    private final AiModelFactory aiModelFactory;
    private final ObjectMapper objectMapper;

    public ResearchAssistantService(ResearchSessionMapper sessionMapper,
                                    ResearchMessageMapper messageMapper,
                                    ToolInvocationMapper invocationMapper,
                                    ResearchToolPlanner toolPlanner,
                                    AiModelFactory aiModelFactory,
                                    ObjectMapper objectMapper) {
        this.sessionMapper = sessionMapper;
        this.messageMapper = messageMapper;
        this.invocationMapper = invocationMapper;
        this.toolPlanner = toolPlanner;
        this.aiModelFactory = aiModelFactory;
        this.objectMapper = objectMapper;
    }

    public ResearchAnswerVO ask(Long userId, Long sessionId, String question) {
        String traceId = currentTraceId();
        ResearchSession session = getOrCreateSession(userId, sessionId, question, traceId);
        saveMessage(session.getId(), "user", question, null);

        List<Evidence> evidences = new ArrayList<>();
        List<ToolInvocation> invocations = new ArrayList<>();
        for (ResearchTool tool : toolPlanner.plan(question)) {
            ToolInvocation invocation = executeTool(
                    tool,
                    new ResearchToolContext(userId, question, 5),
                    session.getId(),
                    traceId,
                    evidences
            );
            invocations.add(invocation);
        }
        assignCitationIndexes(evidences);

        String prompt = buildEvidencePrompt(question, evidences);
        AiModelClient client = aiModelFactory.create();
        String answer = client.chat(SYSTEM_PROMPT, prompt);
        ResearchMessage assistantMessage = saveMessage(
                session.getId(),
                "assistant",
                answer,
                toJson(evidences)
        );
        sessionMapper.updateById(session);

        ResearchAnswerVO result = new ResearchAnswerVO();
        result.setSessionId(session.getId());
        result.setMessageId(assistantMessage.getId());
        result.setAnswer(answer);
        result.setTraceId(traceId);
        result.setEvidences(evidences);
        result.setToolInvocations(invocations);
        return result;
    }

    public List<ResearchSession> sessions(Long userId) {
        return sessionMapper.selectList(
                new LambdaQueryWrapper<ResearchSession>()
                        .eq(ResearchSession::getUserId, userId)
                        .orderByDesc(ResearchSession::getUpdatedAt)
        );
    }

    public List<ResearchMessage> messages(Long userId, Long sessionId) {
        requireOwnedSession(userId, sessionId);
        return messageMapper.selectList(
                new LambdaQueryWrapper<ResearchMessage>()
                        .eq(ResearchMessage::getSessionId, sessionId)
                        .orderByAsc(ResearchMessage::getCreatedAt)
        );
    }

    public List<ToolInvocation> invocations(Long userId, Long sessionId) {
        requireOwnedSession(userId, sessionId);
        return invocationMapper.selectList(
                new LambdaQueryWrapper<ToolInvocation>()
                        .eq(ToolInvocation::getSessionId, sessionId)
                        .orderByAsc(ToolInvocation::getCreatedAt)
        );
    }

    private ToolInvocation executeTool(ResearchTool tool,
                                       ResearchToolContext context,
                                       Long sessionId,
                                       String traceId,
                                       List<Evidence> evidenceCollector) {
        long startedAt = System.nanoTime();
        ToolInvocation invocation = new ToolInvocation();
        invocation.setSessionId(sessionId);
        invocation.setTraceId(traceId);
        invocation.setToolName(tool.name());
        invocation.setInputJson(toJson(Map.of(
                "query", context.getQuery(),
                "maxResults", context.getMaxResults()
        )));
        try {
            List<Evidence> output = tool.execute(context);
            evidenceCollector.addAll(output);
            invocation.setOutputJson(toJson(output));
            invocation.setStatus("SUCCEEDED");
        } catch (Exception e) {
            invocation.setStatus("FAILED");
            invocation.setErrorMessage(rootMessage(e));
            invocation.setOutputJson("[]");
        }
        invocation.setDurationMs((System.nanoTime() - startedAt) / 1_000_000);
        invocationMapper.insert(invocation);
        return invocation;
    }

    private ResearchSession getOrCreateSession(Long userId,
                                               Long sessionId,
                                               String question,
                                               String traceId) {
        if (sessionId != null) {
            return requireOwnedSession(userId, sessionId);
        }
        ResearchSession session = new ResearchSession();
        session.setUserId(userId);
        session.setTitle(question.length() > 80 ? question.substring(0, 80) : question);
        session.setTraceId(traceId);
        sessionMapper.insert(session);
        return session;
    }

    private ResearchSession requireOwnedSession(Long userId, Long sessionId) {
        ResearchSession session = sessionMapper.selectById(sessionId);
        if (session == null || !userId.equals(session.getUserId())) {
            throw new BusinessException("研究会话不存在");
        }
        return session;
    }

    private ResearchMessage saveMessage(Long sessionId,
                                        String role,
                                        String content,
                                        String citationsJson) {
        ResearchMessage message = new ResearchMessage();
        message.setSessionId(sessionId);
        message.setRole(role);
        message.setContent(content);
        message.setCitationsJson(citationsJson);
        messageMapper.insert(message);
        return message;
    }

    private void assignCitationIndexes(List<Evidence> evidences) {
        Map<String, Evidence> unique = new LinkedHashMap<>();
        for (Evidence evidence : evidences) {
            String key = evidence.getSourceUrl() == null
                    ? evidence.getTitle()
                    : evidence.getSourceUrl();
            unique.putIfAbsent(key, evidence);
        }
        evidences.clear();
        evidences.addAll(unique.values());
        for (int index = 0; index < evidences.size(); index++) {
            evidences.get(index).setCitationIndex(index + 1);
        }
    }

    private String buildEvidencePrompt(String question, List<Evidence> evidences) {
        String evidenceText = evidences.stream()
                .map(item -> String.format(
                        "[%d] %s%n来源：%s%n摘要：%s",
                        item.getCitationIndex(),
                        item.getTitle(),
                        item.getSourceUrl(),
                        item.getSnippet()
                ))
                .collect(Collectors.joining("\n\n"));
        return "用户问题：\n" + question + "\n\n检索证据：\n"
                + (evidenceText.isBlank() ? "未检索到可用证据。" : evidenceText);
    }

    private String currentTraceId() {
        String traceId = MDC.get("traceId");
        return traceId == null ? UUID.randomUUID().toString().replace("-", "") : traceId;
    }

    private String toJson(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (Exception e) {
            throw new IllegalStateException("研究助手记录序列化失败", e);
        }
    }

    private String rootMessage(Throwable throwable) {
        Throwable current = throwable;
        while (current.getCause() != null) {
            current = current.getCause();
        }
        String message = current.getClass().getSimpleName() + ": " + current.getMessage();
        return message.length() > 1900 ? message.substring(0, 1900) : message;
    }
}
