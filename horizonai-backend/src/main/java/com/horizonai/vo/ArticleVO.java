package com.horizonai.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
public class ArticleVO {

    private Long id;
    private String title;
    private String summary;
    private String content;
    private String sourceUrl;
    private String sourceType;
    private LocalDate publishDate;
    private Integer importanceRating;
    private LocalDateTime createdAt;
    private List<TagVO> tags;
}
