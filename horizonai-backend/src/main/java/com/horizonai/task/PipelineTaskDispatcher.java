package com.horizonai.task;

import com.horizonai.entity.PipelineTask;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class PipelineTaskDispatcher {

    private final PipelineTaskManager taskManager;
    private final PipelineTaskSubmissionService submissionService;

    public PipelineTaskDispatcher(PipelineTaskManager taskManager,
                                  PipelineTaskSubmissionService submissionService) {
        this.taskManager = taskManager;
        this.submissionService = submissionService;
    }

    public PipelineTask submit(PipelineTaskType taskType,
                               String bizKey,
                               String idempotencyKey,
                               String payloadJson) {
        return submissionService.submit(taskType, bizKey, idempotencyKey, payloadJson);
    }

    public int dispatchDueTasks(int limit) {
        List<PipelineTask> tasks = taskManager.findDispatchable(limit);
        tasks.forEach(task -> submissionService.signal(task.getId()));
        return tasks.size();
    }

    public PipelineTask retryFailed(Long taskId) {
        PipelineTask task = taskManager.getRequired(taskId);
        if (taskManager.requeueFailed(taskId)) {
            submissionService.signal(taskId);
        }
        return taskManager.getRequired(taskId);
    }
}
