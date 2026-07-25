package com.horizonai.task;

import com.horizonai.entity.PipelineTask;
import org.slf4j.MDC;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.UUID;

@Service
public class PipelineTaskDispatcher {

    private final PipelineTaskManager taskManager;
    private final PipelineTaskExecutor taskExecutor;

    public PipelineTaskDispatcher(PipelineTaskManager taskManager,
                                  PipelineTaskExecutor taskExecutor) {
        this.taskManager = taskManager;
        this.taskExecutor = taskExecutor;
    }

    public PipelineTask submit(PipelineTaskType taskType,
                               String bizKey,
                               String idempotencyKey,
                               String payloadJson) {
        String traceId = currentOrNewTraceId();
        PipelineTask task = taskManager.create(
                traceId,
                taskType,
                bizKey,
                idempotencyKey,
                payloadJson,
                3
        );
        if (PipelineTaskStatus.PENDING.name().equals(task.getStatus())
                || PipelineTaskStatus.RETRY_WAIT.name().equals(task.getStatus())) {
            taskExecutor.execute(task.getId());
        }
        return task;
    }

    public int dispatchDueTasks(int limit) {
        List<PipelineTask> tasks = taskManager.findDispatchable(limit);
        tasks.forEach(task -> taskExecutor.execute(task.getId()));
        return tasks.size();
    }

    public PipelineTask retryFailed(Long taskId) {
        PipelineTask task = taskManager.getRequired(taskId);
        if (taskManager.requeueFailed(taskId)) {
            taskExecutor.execute(taskId);
        }
        return taskManager.getRequired(taskId);
    }

    private String currentOrNewTraceId() {
        String traceId = MDC.get("traceId");
        return StringUtils.hasText(traceId)
                ? traceId
                : UUID.randomUUID().toString().replace("-", "");
    }
}
