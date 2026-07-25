package com.horizonai.task;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class PipelineRetryScheduler {

    private static final Logger log = LoggerFactory.getLogger(PipelineRetryScheduler.class);

    private final PipelineTaskManager taskManager;
    private final PipelineTaskDispatcher taskDispatcher;

    public PipelineRetryScheduler(PipelineTaskManager taskManager,
                                  PipelineTaskDispatcher taskDispatcher) {
        this.taskManager = taskManager;
        this.taskDispatcher = taskDispatcher;
    }

    @Scheduled(fixedDelayString = "${pipeline.retry-scan-delay:15000}")
    public void retryDueTasks() {
        int recovered = taskManager.recoverStaleRunningTasks(LocalDateTime.now().minusMinutes(10));
        int dispatched = taskDispatcher.dispatchDueTasks(50);
        if (recovered > 0 || dispatched > 0) {
            log.info("流水线任务扫描完成: recovered={}, dispatched={}", recovered, dispatched);
        }
    }
}
