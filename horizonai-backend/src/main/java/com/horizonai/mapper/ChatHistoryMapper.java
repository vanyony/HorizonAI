package com.horizonai.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.horizonai.entity.ChatHistory;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ChatHistoryMapper extends BaseMapper<ChatHistory> {
}
