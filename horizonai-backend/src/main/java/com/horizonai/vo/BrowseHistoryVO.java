package com.horizonai.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class BrowseHistoryVO {

    private Long articleId;
    private String articleTitle;
    private String sourceType;
    private LocalDateTime browseTime;
}
