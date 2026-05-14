package com.horizonai.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessageVO {

    private Long id;
    private String role;
    private String content;
    private LocalDateTime createdAt;
}
