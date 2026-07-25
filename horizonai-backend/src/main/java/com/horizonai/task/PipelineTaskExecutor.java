package com.horizonai.task;

import com.horizonai.entity.PipelineTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@Component
public class PipelineTaskExecutor {

    private static final Logger log = LoggerFactory.getLogger(PipelineTaskExecutor.class);

    private final PipelineTaskManager taskManager;
    private final Map<PipelineTaskType, PipelineTaskHandler> handlers;

    public PipelineTaskExecutor(PipelineTaskManager taskManager,
                                List<PipelineTaskHandler> handlerList) {
        this.taskManager = taskManager;
        this.handlers = new EnumMap<>(PipelineTaskType.class);
        for (PipelineTaskHandler handler : handlerList) {
            this.handlers.put(handler.taskType(), handler);
        }
    }

    @Async("pipelineTaskExecutor")
    public void execute(Long taskId) {
        if (!taskManager.claim(taskId)) {
            return;
        }

        PipelineTask task = taskManager.getRequired(taskId);
        MDC.put("traceId", task.getTraceId());
        MDC.put("taskId", String.valueOf(taskId));
        try {
            PipelineTaskType taskType = PipelineTaskType.valueOf(task.getTaskType());
            PipelineTaskHandler handler = handlers.get(taskType);
            if (handler == null) {
                throw new IllegalStateException("没有可用的任务处理器: " + taskType);
            }

            log.info("流水线任务开始: type={}, bizKey={}, attempt={}/{}",
                    task.getTaskType(), task.getBizKey(), task.getAttempt(), task.getMaxAttempts());
            handler.handle(task);
            taskManager.markSucceeded(taskId);
            log.info("流水线任务完成: type={}, bizKey={}", task.getTaskType(), task.getBizKey());
        } catch (Exception e) {
            taskManager.markFailed(taskId, e);
            log.error("流水线任务失败: type={}, bizKey={}", task.getTaskType(), task.getBizKey(), e);
        } finally {
            MDC.remove("taskId");
            MDC.remove("traceId");
        }
    }
}
