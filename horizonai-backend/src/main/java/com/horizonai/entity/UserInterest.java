package com.horizonai.entity;

import com.baomidou.mybatisplus.annotation.*;
import java.time.LocalDateTime;

@TableName("user_interests")
public class UserInterest {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private Long tagId;

    private Integer interestLevel;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    // ========== 手动 getter/setter 方法（替代 Lombok @Data） ==========

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public Long getTagId() { return tagId; }
    public void setTagId(Long tagId) { this.tagId = tagId; }

    public Integer getInterestLevel() { return interestLevel; }
    public void setInterestLevel(Integer interestLevel) { this.interestLevel = interestLevel; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
