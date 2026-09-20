package com.horizonai.task;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.horizonai.common.BusinessException;
import com.horizonai.entity.PipelineTask;
import com.horizonai.mapper.PipelineTaskMapper;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class PipelineTaskManager {

    private static final int ERROR_MESSAGE_LIMIT = 1900;

    private final PipelineTaskMapper pipelineTaskMapper;

    public PipelineTaskManager(PipelineTaskMapper pipelineTaskMapper) {
        this.pipelineTaskMapper = pipelineTaskMapper;
    }

    public PipelineTask create(String traceId,
                               PipelineTaskType taskType,
                               String bizKey,
                               String idempotencyKey,
                               String payloadJson,
                               int maxAttempts) {
        PipelineTask existing = findByIdempotencyKey(idempotencyKey);
        if (existing != null) {
            return existing;
        }

        PipelineTask task = new PipelineTask();
        task.setTraceId(traceId);
        task.setTaskType(taskType.name());
        task.setBizKey(bizKey);
        task.setIdempotencyKey(idempotencyKey);
        task.setPayloadJson(payloadJson);
        task.setStatus(PipelineTaskStatus.PENDING.name());
        task.setAttempt(0);
        task.setMaxAttempts(maxAttempts);

        try {
            pipelineTaskMapper.insert(task);
            return task;
        } catch (DuplicateKeyException e) {
            PipelineTask concurrentTask = findByIdempotencyKey(idempotencyKey);
            if (concurrentTask != null) {
                return concurrentTask;
            }
            throw e;
        }
    }

    public PipelineTask getRequired(Long taskId) {
        PipelineTask task = pipelineTaskMapper.selectById(taskId);
        if (task == null) {
            throw new BusinessException("流水线任务不存在");
        }
        return task;
    }

    public boolean claim(Long taskId) {
        LocalDateTime now = LocalDateTime.now();
        return pipelineTaskMapper.update(
                null,
                new LambdaUpdateWrapper<PipelineTask>()
                        .set(PipelineTask::getStatus, PipelineTaskStatus.RUNNING.name())
                        .set(PipelineTask::getStartedAt, now)
                        .set(PipelineTask::getFinishedAt, null)
                        .set(PipelineTask::getErrorMessage, null)
                        .setSql("attempt = attempt + 1")
                        .eq(PipelineTask::getId, taskId)
                        .in(PipelineTask::getStatus,
                                PipelineTaskStatus.PENDING.name(),
                                PipelineTaskStatus.RETRY_WAIT.name())
        ) == 1;
    }

    public void markSucceeded(Long taskId) {
        pipelineTaskMapper.update(
                null,
                new LambdaUpdateWrapper<PipelineTask>()
                        .set(PipelineTask::getStatus, PipelineTaskStatus.SUCCEEDED.name())
                        .set(PipelineTask::getFinishedAt, LocalDateTime.now())
                        .set(PipelineTask::getNextRetryAt, null)
                        .set(PipelineTask::getErrorMessage, null)
                        .eq(PipelineTask::getId, taskId)
                        .eq(PipelineTask::getStatus, PipelineTaskStatus.RUNNING.name())
        );
    }

    public void markFailed(Long taskId, Throwable throwable) {
        PipelineTask task = getRequired(taskId);
        boolean canRetry = task.getAttempt() < task.getMaxAttempts();
        String message = rootMessage(throwable);

        LambdaUpdateWrapper<PipelineTask> update = new LambdaUpdateWrapper<PipelineTask>()
                .set(PipelineTask::getStatus,
                        canRetry ? PipelineTaskStatus.RETRY_WAIT.name() : PipelineTaskStatus.FAILED.name())
                .set(PipelineTask::getErrorMessage, message)
                .set(PipelineTask::getFinishedAt, canRetry ? null : LocalDateTime.now())
                .eq(PipelineTask::getId, taskId)
                .eq(PipelineTask::getStatus, PipelineTaskStatus.RUNNING.name());

        if (canRetry) {
            long delaySeconds = Math.min(300L, 15L * (1L << Math.max(0, task.getAttempt() - 1)));
            update.set(PipelineTask::getNextRetryAt, LocalDateTime.now().plusSeconds(delaySeconds));
        } else {
            update.set(PipelineTask::getNextRetryAt, null);
        }
        pipelineTaskMapper.update(null, update);
    }

    public List<PipelineTask> findDispatchable(int limit) {
        return pipelineTaskMapper.findRedispatchable(Math.max(1, Math.min(limit, 100)));
    }

    public int recoverStaleRunningTasks(LocalDateTime staleBefore) {
        return pipelineTaskMapper.update(
                null,
                new LambdaUpdateWrapper<PipelineTask>()
                        .set(PipelineTask::getStatus, PipelineTaskStatus.RETRY_WAIT.name())
                        .set(PipelineTask::getNextRetryAt, LocalDateTime.now())
                        .set(PipelineTask::getErrorMessage, "任务执行进程中断，已重新进入调度队列")
                        .eq(PipelineTask::getStatus, PipelineTaskStatus.RUNNING.name())
                        .lt(PipelineTask::getUpdatedAt, staleBefore)
        );
    }

    public Page<PipelineTask> page(int pageNum, int pageSize, String status, String taskType) {
        Page<PipelineTask> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<PipelineTask> query = new LambdaQueryWrapper<>();
        if (status != null && !status.isBlank()) {
            query.eq(PipelineTask::getStatus, status);
        }
        if (taskType != null && !taskType.isBlank()) {
            query.eq(PipelineTask::getTaskType, taskType);
        }
        query.orderByDesc(PipelineTask::getCreatedAt);
        return pipelineTaskMapper.selectPage(page, query);
    }

    public List<PipelineTask> findByTraceId(String traceId) {
        return pipelineTaskMapper.selectList(
                new LambdaQueryWrapper<PipelineTask>()
                        .eq(PipelineTask::getTraceId, traceId)
                        .orderByAsc(PipelineTask::getCreatedAt)
        );
    }

    public boolean requeueFailed(Long taskId) {
        return pipelineTaskMapper.update(
                null,
                new LambdaUpdateWrapper<PipelineTask>()
                        .set(PipelineTask::getStatus, PipelineTaskStatus.RETRY_WAIT.name())
                        .set(PipelineTask::getNextRetryAt, LocalDateTime.now())
                        .set(PipelineTask::getFinishedAt, null)
                        .set(PipelineTask::getErrorMessage, null)
                        .eq(PipelineTask::getId, taskId)
                        .eq(PipelineTask::getStatus, PipelineTaskStatus.FAILED.name())
        ) == 1;
    }

    private PipelineTask findByIdempotencyKey(String idempotencyKey) {
        return pipelineTaskMapper.selectOne(
                new LambdaQueryWrapper<PipelineTask>()
                        .eq(PipelineTask::getIdempotencyKey, idempotencyKey)
                        .last("LIMIT 1")
        );
    }

    private String rootMessage(Throwable throwable) {
        Throwable current = throwable;
        while (current.getCause() != null) {
            current = current.getCause();
        }
        String message = current.getClass().getSimpleName() + ": " + current.getMessage();
        if (message.length() > ERROR_MESSAGE_LIMIT) {
            return message.substring(0, ERROR_MESSAGE_LIMIT);
        }
        return message;
    }
}
