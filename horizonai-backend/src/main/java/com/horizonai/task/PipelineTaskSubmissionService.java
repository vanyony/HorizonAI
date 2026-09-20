package com.horizonai.task;

import com.horizonai.entity.PipelineTask;
import org.slf4j.MDC;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.UUID;

@Service
public class PipelineTaskSubmissionService {
    private final PipelineTaskManager taskManager;
    private final PipelineOutboxManager outboxManager;

    public PipelineTaskSubmissionService(PipelineTaskManager taskManager,
                                         PipelineOutboxManager outboxManager) {
        this.taskManager = taskManager;
        this.outboxManager = outboxManager;
    }

    @Transactional
    public PipelineTask submit(PipelineTaskType taskType, String bizKey,
                               String idempotencyKey, String payloadJson) {
        PipelineTask task = taskManager.create(currentOrNewTraceId(), taskType, bizKey,
                idempotencyKey, payloadJson, 3);
        if (PipelineTaskStatus.PENDING.name().equals(task.getStatus())
                || PipelineTaskStatus.RETRY_WAIT.name().equals(task.getStatus())) {
            outboxManager.enqueue(task.getId());
        }
        return task;
    }

    @Transactional
    public void signal(Long taskId) {
        outboxManager.enqueue(taskId);
    }

    private String currentOrNewTraceId() {
        String traceId = MDC.get("traceId");
        return StringUtils.hasText(traceId) ? traceId : UUID.randomUUID().toString().replace("-", "");
    }
}
