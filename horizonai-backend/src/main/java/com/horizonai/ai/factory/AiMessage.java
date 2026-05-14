package com.horizonai.ai.factory;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AiMessage {
    private String role;
    private String content;
}
