package com.horizonai.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("daily_digests")
public class DailyDigest {

    @TableId(type = IdType.AUTO)
    private Long id;

    private LocalDate digestDate;

    private String title;

    private String summary;

    private String articleIds;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
