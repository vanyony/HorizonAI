package com.horizonai.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.horizonai.common.Result;
import com.horizonai.entity.PipelineTask;
import com.horizonai.task.PipelineTaskDispatcher;
import com.horizonai.task.PipelineTaskManager;
import com.horizonai.task.PipelineTaskType;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;
import java.util.List;

@RestController
@RequestMapping("/api/admin/pipeline")
public class PipelineAdminController {

    private final PipelineTaskDispatcher taskDispatcher;
    private final PipelineTaskManager taskManager;

    public PipelineAdminController(PipelineTaskDispatcher taskDispatcher,
                                   PipelineTaskManager taskManager) {
        this.taskDispatcher = taskDispatcher;
        this.taskManager = taskManager;
    }

    @PostMapping("/github-sync")
    public Result<PipelineTask> syncGitHub(
            @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey) {
        String requestKey = StringUtils.hasText(idempotencyKey)
                ? idempotencyKey
                : UUID.randomUUID().toString();
        PipelineTask task = taskDispatcher.submit(
                PipelineTaskType.COLLECT_GITHUB,
                "manual",
                "github-sync:manual:" + requestKey,
                "{}"
        );
        return Result.success(task);
    }

    @PostMapping("/sync/{source}")
    public Result<PipelineTask> syncSource(
            @PathVariable String source,
            @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey) {
        PipelineTaskType taskType;
        switch (source.toUpperCase()) {
            case "GITHUB":
                taskType = PipelineTaskType.COLLECT_GITHUB;
                break;
            case "RSS":
                taskType = PipelineTaskType.COLLECT_RSS;
                break;
            case "DEMO":
                taskType = PipelineTaskType.COLLECT_DEMO;
                break;
            default:
                throw new IllegalArgumentException("不支持的数据源: " + source);
        }
        String requestKey = StringUtils.hasText(idempotencyKey)
                ? idempotencyKey
                : UUID.randomUUID().toString();
        PipelineTask task = taskDispatcher.submit(
                taskType,
                source.toUpperCase(),
                "source-sync:" + source.toLowerCase() + ":" + requestKey,
                "{}"
        );
        return Result.success(task);
    }

    @GetMapping("/tasks")
    public Result<Page<PipelineTask>> tasks(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String taskType) {
        return Result.success(taskManager.page(page, size, status, taskType));
    }

    @GetMapping("/tasks/{taskId}")
    public Result<PipelineTask> taskDetail(@PathVariable Long taskId) {
        return Result.success(taskManager.getRequired(taskId));
    }

    @GetMapping("/traces/{traceId}")
    public Result<List<PipelineTask>> traceTasks(@PathVariable String traceId) {
        return Result.success(taskManager.findByTraceId(traceId));
    }

    @PostMapping("/tasks/{taskId}/retry")
    public Result<PipelineTask> retryTask(@PathVariable Long taskId) {
        return Result.success(taskDispatcher.retryFailed(taskId));
    }
}
