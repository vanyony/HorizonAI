package com.horizonai.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class TagVO {

    private Long id;
    private String name;
    private String description;
    private LocalDateTime createdAt;
}
