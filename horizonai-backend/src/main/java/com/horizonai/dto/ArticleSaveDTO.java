package com.horizonai.dto;

import lombok.Data;
import javax.validation.constraints.NotBlank;
import java.time.LocalDate;
import java.util.List;

@Data
public class ArticleSaveDTO {

    @NotBlank(message = "标题不能为空")
    private String title;

    private String summary;

    private String content;

    private String sourceUrl;

    @NotBlank(message = "来源类型不能为空")
    private String sourceType;

    private LocalDate publishDate;

    private List<Long> tagIds;
}
