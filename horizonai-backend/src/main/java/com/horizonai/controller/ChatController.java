package com.horizonai.controller;

import com.horizonai.common.Result;
import com.horizonai.dto.ChatSendRequest;
import com.horizonai.service.ChatService;
import com.horizonai.vo.ChatMessageVO;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;

    @PostMapping("/send")
    public Result<ChatMessageVO> send(Authentication authentication,
                                      @Valid @RequestBody ChatSendRequest request) {
        Long userId = (Long) authentication.getPrincipal();
        return Result.success(chatService.sendMessage(userId, request.getContent()));
    }

    @GetMapping("/history")
    public Result<List<ChatMessageVO>> history(Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        return Result.success(chatService.getHistory(userId));
    }
}
