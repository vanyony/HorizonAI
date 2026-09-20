package com.horizonai.messaging;

import com.horizonai.task.PipelineTaskExecutor;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "pipeline.dispatch-mode", havingValue = "rocketmq")
@RocketMQMessageListener(
        topic = "${horizon.messaging.pipeline-topic}",
        consumerGroup = "${horizon.messaging.consumer-group}"
)
public class RocketMqPipelineConsumer implements RocketMQListener<String> {
    private final PipelineTaskExecutor taskExecutor;

    public RocketMqPipelineConsumer(PipelineTaskExecutor taskExecutor) {
        this.taskExecutor = taskExecutor;
    }

    @Override
    public void onMessage(String message) {
        taskExecutor.executeNow(Long.valueOf(message));
        // 业务失败由 PipelineTask 状态机记录；返回后 ACK，定时器到期后重新发布。
    }
}
