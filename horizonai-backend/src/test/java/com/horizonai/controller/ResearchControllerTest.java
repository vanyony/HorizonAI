package com.horizonai.controller;

import com.horizonai.service.ResearchAssistantService;
import com.horizonai.vo.ResearchAnswerVO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;


import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;

class ResearchControllerTest {

    private ResearchAssistantService service;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        service = mock(ResearchAssistantService.class);
        LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();
        mockMvc = standaloneSetup(new ResearchController(service))
                .setValidator(validator)
                .build();
    }

    @Test
    void askUsesAuthenticatedPrincipalAndReturnsWrappedAnswer() throws Exception {
        ResearchAnswerVO answer = new ResearchAnswerVO();
        answer.setSessionId(22L);
        answer.setAnswer("answer");
        when(service.ask(7L, 9L, "Java 虚拟线程适合什么场景？")).thenReturn(answer);

        mockMvc.perform(post("/api/research/ask")
                        .principal(new TestingAuthenticationToken(7L, null))
                        .contentType(APPLICATION_JSON)
                        .content("{\"sessionId\":9,\"question\":\"Java 虚拟线程适合什么场景？\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.sessionId").value(22))
                .andExpect(jsonPath("$.data.answer").value("answer"));

        verify(service).ask(eq(7L), eq(9L), eq("Java 虚拟线程适合什么场景？"));
    }

    @Test
    void blankQuestionIsRejectedBeforeServiceCall() throws Exception {
        mockMvc.perform(post("/api/research/ask")
                        .principal(new TestingAuthenticationToken(7L, null))
                        .contentType(APPLICATION_JSON)
                        .content("{\"question\":\"   \"}"))
                .andExpect(status().isBadRequest());

        verify(service, never()).ask(eq(7L), org.mockito.ArgumentMatchers.<Long>isNull(), eq("   "));
    }
}
