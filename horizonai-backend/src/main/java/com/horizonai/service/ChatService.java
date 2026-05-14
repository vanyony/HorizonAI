package com.horizonai.service;

import com.horizonai.vo.ChatMessageVO;

import java.util.List;

public interface ChatService {

    /**
     * 发送消息并获取 AI 回复
     */
    ChatMessageVO sendMessage(Long userId, String content);

    /**
     * 获取用户对话历史
     */
    List<ChatMessageVO> getHistory(Long userId);
}
