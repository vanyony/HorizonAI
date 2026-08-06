package com.horizonai.service;

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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.slf4j.MDC;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ResearchAssistantServiceTest {

    private ResearchSessionMapper sessionMapper;
    private ResearchMessageMapper messageMapper;
    private ToolInvocationMapper invocationMapper;
    private ResearchToolPlanner toolPlanner;
    private AiModelFactory aiModelFactory;
    private AiModelClient aiModelClient;
    private ResearchAssistantService service;

    @BeforeEach
    void setUp() {
        sessionMapper = mock(ResearchSessionMapper.class);
        messageMapper = mock(ResearchMessageMapper.class);
        invocationMapper = mock(ToolInvocationMapper.class);
        toolPlanner = mock(ResearchToolPlanner.class);
        aiModelFactory = mock(AiModelFactory.class);
        aiModelClient = mock(AiModelClient.class);
        service = new ResearchAssistantService(
                sessionMapper,
                messageMapper,
                invocationMapper,
                toolPlanner,
                aiModelFactory,
                new ObjectMapper()
        );
    }

    @Test
    void askDeduplicatesEvidenceAssignsCitationsAndRecordsTrace() {
        stubNewSession(21L);
        stubMessageIds();
        when(toolPlanner.plan("Java 虚拟线程适合什么场景？")).thenReturn(List.of(
                tool("site_search", List.of(
                        evidence("Virtual Threads", "第一份摘要", "https://example.test/java"),
                        evidence("Duplicate", "重复来源", "https://example.test/duplicate")
                )),
                tool("interest_match", List.of(
                        evidence("Duplicate", "重复来源的新版本", "https://example.test/duplicate"),
                        evidence("Interest", "第二份摘要", "https://example.test/interest")
                ))
        ));
        when(aiModelFactory.create()).thenReturn(aiModelClient);
        when(aiModelClient.chat(anyString(), anyString())).thenReturn("基于证据的回答");

        MDC.put("traceId", "trace-12345678");
        ResearchAnswerVO result;
        try {
            result = service.ask(7L, null, "Java 虚拟线程适合什么场景？");
        } finally {
            MDC.remove("traceId");
        }

        assertEquals(21L, result.getSessionId());
        assertEquals("trace-12345678", result.getTraceId());
        assertEquals("基于证据的回答", result.getAnswer());
        assertEquals(3, result.getEvidences().size());
        assertEquals(1, result.getEvidences().get(0).getCitationIndex());
        assertEquals(2, result.getEvidences().get(1).getCitationIndex());
        assertEquals(3, result.getEvidences().get(2).getCitationIndex());
        assertEquals(2, result.getToolInvocations().size());
        assertTrue(result.getToolInvocations().stream()
                .allMatch(item -> "SUCCEEDED".equals(item.getStatus())));
        assertTrue(result.getToolInvocations().stream()
                .allMatch(item -> "trace-12345678".equals(item.getTraceId())));

        ArgumentCaptor<String> promptCaptor = ArgumentCaptor.forClass(String.class);
        verify(aiModelClient).chat(anyString(), promptCaptor.capture());
        assertTrue(promptCaptor.getValue().contains("[1]"));
        assertTrue(promptCaptor.getValue().contains("https://example.test/java"));

        ArgumentCaptor<ResearchMessage> messageCaptor =
                ArgumentCaptor.forClass(ResearchMessage.class);
        verify(messageMapper, org.mockito.Mockito.times(2)).insert(messageCaptor.capture());
        ResearchMessage assistantMessage = messageCaptor.getAllValues().get(1);
        assertEquals("assistant", assistantMessage.getRole());
        assertTrue(assistantMessage.getCitationsJson().contains("citationIndex"));
    }

    @Test
    void failedToolIsRecordedAndDoesNotBlockAnswerGeneration() {
        stubNewSession(22L);
        stubMessageIds();
        when(toolPlanner.plan("失败工具测试")).thenReturn(List.of(
                failingTool("site_search", new IllegalStateException("search unavailable"))
        ));
        when(aiModelFactory.create()).thenReturn(aiModelClient);
        when(aiModelClient.chat(anyString(), anyString())).thenReturn("没有足够证据");

        ResearchAnswerVO result = service.ask(7L, null, "失败工具测试");

        assertEquals(1, result.getToolInvocations().size());
        ToolInvocation invocation = result.getToolInvocations().get(0);
        assertEquals("FAILED", invocation.getStatus());
        assertEquals("[]", invocation.getOutputJson());
        assertTrue(invocation.getErrorMessage().contains("IllegalStateException"));
        assertTrue(result.getEvidences().isEmpty());

        ArgumentCaptor<String> promptCaptor = ArgumentCaptor.forClass(String.class);
        verify(aiModelClient).chat(anyString(), promptCaptor.capture());
        assertTrue(promptCaptor.getValue().contains("未检索到可用证据"));
    }

    @Test
    void refusesAccessToAnotherUsersSession() {
        ResearchSession session = new ResearchSession();
        session.setId(31L);
        session.setUserId(9L);
        when(sessionMapper.selectById(31L)).thenReturn(session);

        assertThrows(BusinessException.class, () -> service.messages(7L, 31L));
        verify(messageMapper, never()).selectList(any());
    }

    private void stubNewSession(Long sessionId) {
        doAnswer(invocation -> {
            ResearchSession session = invocation.getArgument(0);
            session.setId(sessionId);
            return 1;
        }).when(sessionMapper).insert(any(ResearchSession.class));
    }

    private void stubMessageIds() {
        long[] nextId = {100L};
        doAnswer(invocation -> {
            ResearchMessage message = invocation.getArgument(0);
            message.setId(nextId[0]++);
            return 1;
        }).when(messageMapper).insert(any(ResearchMessage.class));
    }

    private ResearchTool tool(String name, List<Evidence> output) {
        return new ResearchTool() {
            @Override
            public String name() {
                return name;
            }

            @Override
            public List<Evidence> execute(ResearchToolContext context) {
                return output;
            }
        };
    }

    private ResearchTool failingTool(String name, RuntimeException failure) {
        return new ResearchTool() {
            @Override
            public String name() {
                return name;
            }

            @Override
            public List<Evidence> execute(ResearchToolContext context) {
                throw failure;
            }
        };
    }

    private Evidence evidence(String title, String snippet, String sourceUrl) {
        return new Evidence(title, snippet, sourceUrl, "test");
    }
}
