package com.horizonai.task;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.horizonai.entity.PipelineOutboxEvent;
import com.horizonai.mapper.PipelineOutboxMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class PipelineOutboxManager {
    private static final int ERROR_LIMIT = 1900;
    private final PipelineOutboxMapper mapper;
    private final Duration retryDelay;

    public PipelineOutboxManager(PipelineOutboxMapper mapper,
                                 @Value("${pipeline.outbox.retry-delay:5s}") Duration retryDelay) {
        this.mapper = mapper;
        this.retryDelay = retryDelay;
    }

    public void enqueue(Long taskId) {
        mapper.enqueue(taskId);
    }

    public List<PipelineOutboxEvent> findReady(int limit) {
        return mapper.selectList(new LambdaQueryWrapper<PipelineOutboxEvent>()
                .eq(PipelineOutboxEvent::getStatus, "PENDING")
                .le(PipelineOutboxEvent::getNextAttemptAt, LocalDateTime.now())
                .orderByAsc(PipelineOutboxEvent::getCreatedAt)
                .last("LIMIT " + Math.max(1, Math.min(limit, 100))));
    }

    public boolean claim(Long eventId) {
        return mapper.update(null, new LambdaUpdateWrapper<PipelineOutboxEvent>()
                .set(PipelineOutboxEvent::getStatus, "PUBLISHING")
                .setSql("publish_attempt = publish_attempt + 1")
                .eq(PipelineOutboxEvent::getId, eventId)
                .eq(PipelineOutboxEvent::getStatus, "PENDING")) == 1;
    }

    public void markPublished(Long eventId) {
        mapper.update(null, new LambdaUpdateWrapper<PipelineOutboxEvent>()
                .set(PipelineOutboxEvent::getStatus, "PUBLISHED")
                .set(PipelineOutboxEvent::getPublishedAt, LocalDateTime.now())
                .set(PipelineOutboxEvent::getLastError, null)
                .eq(PipelineOutboxEvent::getId, eventId)
                .eq(PipelineOutboxEvent::getStatus, "PUBLISHING"));
    }

    public void markPublishFailed(Long eventId, Throwable throwable) {
        String message = throwable.getClass().getSimpleName() + ": " + throwable.getMessage();
        if (message.length() > ERROR_LIMIT) message = message.substring(0, ERROR_LIMIT);
        mapper.update(null, new LambdaUpdateWrapper<PipelineOutboxEvent>()
                .set(PipelineOutboxEvent::getStatus, "PENDING")
                .set(PipelineOutboxEvent::getNextAttemptAt, LocalDateTime.now().plus(retryDelay))
                .set(PipelineOutboxEvent::getLastError, message)
                .eq(PipelineOutboxEvent::getId, eventId)
                .eq(PipelineOutboxEvent::getStatus, "PUBLISHING"));
    }

    public int recoverStalePublishing(LocalDateTime staleBefore) {
        return mapper.update(null, new LambdaUpdateWrapper<PipelineOutboxEvent>()
                .set(PipelineOutboxEvent::getStatus, "PENDING")
                .set(PipelineOutboxEvent::getNextAttemptAt, LocalDateTime.now())
                .set(PipelineOutboxEvent::getLastError, "发布进程中断，等待重新投递")
                .eq(PipelineOutboxEvent::getStatus, "PUBLISHING")
                .lt(PipelineOutboxEvent::getUpdatedAt, staleBefore));
    }
}
