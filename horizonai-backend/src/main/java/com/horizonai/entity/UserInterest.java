package com.horizonai.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("user_interests")
public class UserInterest {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private Long tagId;

    private Integer interestLevel;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
