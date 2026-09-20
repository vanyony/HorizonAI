package com.horizonai.messaging;

import com.horizonai.task.PipelineTaskExecutor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "pipeline.dispatch-mode", havingValue = "local", matchIfMissing = true)
public class LocalPipelineMessagePublisher implements PipelineMessagePublisher {
    private final PipelineTaskExecutor taskExecutor;

    public LocalPipelineMessagePublisher(PipelineTaskExecutor taskExecutor) {
        this.taskExecutor = taskExecutor;
    }

    @Override
    public void publish(Long taskId) {
        taskExecutor.execute(taskId);
    }
}
