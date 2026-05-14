package com.horizonai.dto;

import lombok.Data;
import javax.validation.constraints.NotBlank;

@Data
public class ChatSendRequest {

    @NotBlank(message = "消息内容不能为空")
    private String content;
}
