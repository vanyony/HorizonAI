package com.horizonai.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.horizonai.entity.PipelineOutboxEvent;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface PipelineOutboxMapper extends BaseMapper<PipelineOutboxEvent> {
    @Insert("INSERT INTO pipeline_outbox(task_id,event_type,status,publish_attempt,next_attempt_at) " +
            "VALUES(#{taskId},'PIPELINE_TASK_READY','PENDING',0,NOW()) " +
            "ON DUPLICATE KEY UPDATE status='PENDING', next_attempt_at=NOW(), last_error=NULL")
    int enqueue(@Param("taskId") Long taskId);
}
