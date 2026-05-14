package com.horizonai.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("articles")
public class Article {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String title;

    private String summary;

    private String content;

    private String sourceUrl;

    private String sourceType;

    private LocalDate publishDate;

    private Integer importanceRating;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
