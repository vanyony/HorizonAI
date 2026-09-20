package com.horizonai.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.horizonai.entity.PipelineTask;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface PipelineTaskMapper extends BaseMapper<PipelineTask> {
    @Select("SELECT t.* FROM pipeline_tasks t " +
            "LEFT JOIN pipeline_outbox o ON o.task_id = t.id " +
            "WHERE ((t.status = 'RETRY_WAIT' AND t.next_retry_at <= NOW()) " +
            "OR (t.status = 'PENDING' AND (o.id IS NULL OR o.updated_at <= DATE_SUB(NOW(), INTERVAL 1 MINUTE)))) " +
            "ORDER BY t.created_at ASC LIMIT #{limit}")
    List<PipelineTask> findRedispatchable(@Param("limit") int limit);
}
