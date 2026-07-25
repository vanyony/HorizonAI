package com.horizonai.controller;

import com.horizonai.common.Result;
import com.horizonai.dto.ResearchAskRequest;
import com.horizonai.entity.ResearchMessage;
import com.horizonai.entity.ResearchSession;
import com.horizonai.entity.ToolInvocation;
import com.horizonai.service.ResearchAssistantService;
import com.horizonai.vo.ResearchAnswerVO;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/research")
public class ResearchController {

    private final ResearchAssistantService researchAssistantService;

    public ResearchController(ResearchAssistantService researchAssistantService) {
        this.researchAssistantService = researchAssistantService;
    }

    @PostMapping("/ask")
    public Result<ResearchAnswerVO> ask(Authentication authentication,
                                        @Valid @RequestBody ResearchAskRequest request) {
        Long userId = (Long) authentication.getPrincipal();
        return Result.success(researchAssistantService.ask(
                userId,
                request.getSessionId(),
                request.getQuestion()
        ));
    }

    @GetMapping("/sessions")
    public Result<List<ResearchSession>> sessions(Authentication authentication) {
        return Result.success(researchAssistantService.sessions(
                (Long) authentication.getPrincipal()));
    }

    @GetMapping("/sessions/{sessionId}/messages")
    public Result<List<ResearchMessage>> messages(Authentication authentication,
                                                   @PathVariable Long sessionId) {
        return Result.success(researchAssistantService.messages(
                (Long) authentication.getPrincipal(), sessionId));
    }

    @GetMapping("/sessions/{sessionId}/tools")
    public Result<List<ToolInvocation>> tools(Authentication authentication,
                                              @PathVariable Long sessionId) {
        return Result.success(researchAssistantService.invocations(
                (Long) authentication.getPrincipal(), sessionId));
    }
}
