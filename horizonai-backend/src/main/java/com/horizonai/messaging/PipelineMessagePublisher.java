package com.horizonai.messaging;

public interface PipelineMessagePublisher {
    void publish(Long taskId);
}
