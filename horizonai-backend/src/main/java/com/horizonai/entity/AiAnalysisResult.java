package com.horizonai.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("ai_analysis_results")
public class AiAnalysisResult {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long articleId;

    private Integer importanceRating;

    private String targetAudience;

    private String industryImpact;

    private String learningSuggestions;

    private String summary;

    private String rawResponse;

    private String modelUsed;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
