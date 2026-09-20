package com.horizonai.messaging;

import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "pipeline.dispatch-mode", havingValue = "rocketmq")
public class RocketMqPipelineMessagePublisher implements PipelineMessagePublisher {
    private final RocketMQTemplate rocketMQTemplate;
    private final String topic;

    public RocketMqPipelineMessagePublisher(RocketMQTemplate rocketMQTemplate,
                                            @Value("${horizon.messaging.pipeline-topic}") String topic) {
        this.rocketMQTemplate = rocketMQTemplate;
        this.topic = topic;
    }

    @Override
    public void publish(Long taskId) {
        rocketMQTemplate.syncSend(topic, String.valueOf(taskId));
    }
}
