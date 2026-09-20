package com.horizonai.task;

import com.horizonai.entity.PipelineOutboxEvent;
import com.horizonai.messaging.PipelineMessagePublisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class PipelineOutboxRelay {
    private static final Logger log = LoggerFactory.getLogger(PipelineOutboxRelay.class);
    private final PipelineOutboxManager outboxManager;
    private final PipelineMessagePublisher publisher;
    private final int batchSize;

    public PipelineOutboxRelay(PipelineOutboxManager outboxManager,
                               PipelineMessagePublisher publisher,
                               @Value("${pipeline.outbox.batch-size:50}") int batchSize) {
        this.outboxManager = outboxManager;
        this.publisher = publisher;
        this.batchSize = batchSize;
    }

    @Scheduled(fixedDelayString = "${pipeline.outbox.relay-delay:1000}")
    public void relay() {
        outboxManager.recoverStalePublishing(LocalDateTime.now().minusMinutes(1));
        for (PipelineOutboxEvent event : outboxManager.findReady(batchSize)) {
            if (!outboxManager.claim(event.getId())) continue;
            try {
                publisher.publish(event.getTaskId());
                outboxManager.markPublished(event.getId());
            } catch (Exception e) {
                outboxManager.markPublishFailed(event.getId(), e);
                log.warn("流水线任务消息发布失败: taskId={}", event.getTaskId(), e);
            }
        }
    }
}
