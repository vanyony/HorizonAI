package com.horizonai.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.horizonai.ai.factory.AiMessage;
import com.horizonai.ai.factory.AiModelClient;
import com.horizonai.ai.factory.AiModelFactory;
import com.horizonai.entity.ChatHistory;
import com.horizonai.mapper.ChatHistoryMapper;
import com.horizonai.service.ChatService;
import com.horizonai.vo.ChatMessageVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatServiceImpl implements ChatService {

    private static final String SYSTEM_PROMPT =
            "你是「观澜」，一位专业的技术趋势顾问。你的职责是帮助用户理解互联网技术趋势、"
                    + "分析技术新闻的价值、推荐学习路径。\n\n"
                    + "规则：\n"
                    + "1. 只回答与技术趋势、编程学习、技术选型相关的问题\n"
                    + "2. 如果用户的问题与技术完全无关，礼貌地引导回技术话题\n"
                    + "3. 回答要简洁、专业、有洞察力\n"
                    + "4. 用中文回答";

    private final ChatHistoryMapper chatHistoryMapper;
    private final AiModelFactory aiModelFactory;

    @Override
    public ChatMessageVO sendMessage(Long userId, String content) {
        // 保存用户消息
        ChatHistory userMsg = new ChatHistory();
        userMsg.setUserId(userId);
        userMsg.setRole("user");
        userMsg.setContent(content);
        chatHistoryMapper.insert(userMsg);

        // 加载最近 10 条历史
        List<ChatHistory> history = chatHistoryMapper.selectList(
                new LambdaQueryWrapper<ChatHistory>()
                        .eq(ChatHistory::getUserId, userId)
                        .orderByAsc(ChatHistory::getCreatedAt)
                        .last("LIMIT 10")  // 实际上我们需要最近的但按时间排序
        );

        // 构建多轮对话消息
        List<AiMessage> messages = new ArrayList<>();
        for (ChatHistory h : history) {
            messages.add(new AiMessage(h.getRole(), h.getContent()));
        }

        // 调用 AI
        AiModelClient client = aiModelFactory.create();
        String reply = client.chatWithHistory(SYSTEM_PROMPT, messages);

        // 保存 AI 回复
        ChatHistory aiMsg = new ChatHistory();
        aiMsg.setUserId(userId);
        aiMsg.setRole("assistant");
        aiMsg.setContent(reply);
        chatHistoryMapper.insert(aiMsg);

        return ChatMessageVO.builder()
                .id(aiMsg.getId())
                .role("assistant")
                .content(reply)
                .createdAt(aiMsg.getCreatedAt())
                .build();
    }

    @Override
    public List<ChatMessageVO> getHistory(Long userId) {
        List<ChatHistory> history = chatHistoryMapper.selectList(
                new LambdaQueryWrapper<ChatHistory>()
                        .eq(ChatHistory::getUserId, userId)
                        .orderByAsc(ChatHistory::getCreatedAt)
        );
        return history.stream()
                .map(h -> ChatMessageVO.builder()
                        .id(h.getId())
                        .role(h.getRole())
                        .content(h.getContent())
                        .createdAt(h.getCreatedAt())
                        .build())
                .collect(Collectors.toList());
    }
}
