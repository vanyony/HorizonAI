package com.horizonai.task;

import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.horizonai.entity.PipelineTask;
import com.horizonai.mapper.PipelineTaskMapper;
import org.apache.ibatis.builder.MapperBuilderAssistant;
import org.apache.ibatis.session.Configuration;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DuplicateKeyException;

import org.mockito.ArgumentCaptor;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SuppressWarnings("unchecked")
class PipelineTaskManagerTest {

    @BeforeAll
    static void initializeMybatisPlusLambdaMetadata() {
        TableInfoHelper.initTableInfo(
                new MapperBuilderAssistant(new Configuration(), "pipeline-task-test"),
                PipelineTask.class
        );
    }

    @Test
    void createInitializesRecoverableTaskState() {
        PipelineTaskMapper mapper = mock(PipelineTaskMapper.class);
        when(mapper.selectOne(any())).thenReturn(null);
        when(mapper.insert(any())).thenAnswer(invocation -> {
            PipelineTask task = invocation.getArgument(0);
            task.setId(101L);
            return 1;
        });
        PipelineTaskManager manager = new PipelineTaskManager(mapper);

        PipelineTask task = manager.create(
                "trace-12345678",
                PipelineTaskType.COLLECT_DEMO,
                "demo",
                "demo:2026-07-25",
                "{}",
                3
        );

        assertEquals(101L, task.getId());
        assertAll(
                () -> assertEquals("trace-12345678", task.getTraceId()),
                () -> assertEquals(PipelineTaskType.COLLECT_DEMO.name(), task.getTaskType()),
                () -> assertEquals("demo", task.getBizKey()),
                () -> assertEquals("demo:2026-07-25", task.getIdempotencyKey()),
                () -> assertEquals("{}", task.getPayloadJson()),
                () -> assertEquals(PipelineTaskStatus.PENDING.name(), task.getStatus()),
                () -> assertEquals(0, task.getAttempt()),
                () -> assertEquals(3, task.getMaxAttempts())
        );
    }

    @Test
    void sameIdempotencyKeyReturnsExistingTask() {
        PipelineTaskMapper mapper = mock(PipelineTaskMapper.class);
        PipelineTask existing = new PipelineTask();
        existing.setId(9L);
        existing.setStatus(PipelineTaskStatus.SUCCEEDED.name());
        when(mapper.selectOne(any())).thenReturn(existing);
        PipelineTaskManager manager = new PipelineTaskManager(mapper);

        PipelineTask result = manager.create(
                "trace-12345678",
                PipelineTaskType.COLLECT_DEMO,
                "demo",
                "same-key",
                "{}",
                3
        );

        assertEquals(9L, result.getId());
    }

    @Test
    void duplicateInsertReturnsTaskCreatedByConcurrentRequest() {
        PipelineTaskMapper mapper = mock(PipelineTaskMapper.class);
        PipelineTask concurrentTask = new PipelineTask();
        concurrentTask.setId(12L);
        concurrentTask.setStatus(PipelineTaskStatus.PENDING.name());
        when(mapper.selectOne(any())).thenReturn(null, concurrentTask);
        when(mapper.insert(any())).thenThrow(new DuplicateKeyException("duplicate idempotency key"));
        PipelineTaskManager manager = new PipelineTaskManager(mapper);

        PipelineTask result = manager.create(
                "trace-12345678",
                PipelineTaskType.ANALYZE_ARTICLE,
                "article-1",
                "article-1:hash:model:prompt-v1",
                "{}",
                3
        );

        assertSame(concurrentTask, result);
    }

    @Test
    void duplicateInsertIsRethrownWhenConcurrentTaskCannotBeRead() {
        PipelineTaskMapper mapper = mock(PipelineTaskMapper.class);
        when(mapper.selectOne(any())).thenReturn(null);
        DuplicateKeyException duplicate = new DuplicateKeyException("duplicate idempotency key");
        when(mapper.insert(any())).thenThrow(duplicate);
        PipelineTaskManager manager = new PipelineTaskManager(mapper);

        assertSame(duplicate, assertThrows(DuplicateKeyException.class, () -> manager.create(
                "trace-12345678",
                PipelineTaskType.ANALYZE_ARTICLE,
                "article-1",
                "same-key",
                "{}",
                3
        )));
    }

    @Test
    void claimReportsWhetherPendingOrRetryTaskWasClaimed() {
        PipelineTaskMapper mapper = mock(PipelineTaskMapper.class);
        when(mapper.update(isNull(), any())).thenReturn(1, 0);
        PipelineTaskManager manager = new PipelineTaskManager(mapper);

        assertTrue(manager.claim(101L));
        assertFalse(manager.claim(102L));
        ArgumentCaptor<LambdaUpdateWrapper<PipelineTask>> wrapperCaptor =
                ArgumentCaptor.forClass(LambdaUpdateWrapper.class);
        verify(mapper, org.mockito.Mockito.times(2)).update(isNull(), wrapperCaptor.capture());
        assertTrue(wrapperCaptor.getAllValues().get(0).getParamNameValuePairs()
                .containsValue(PipelineTaskStatus.RUNNING.name()));
    }

    @Test
    void markFailedUsesRetryStateBeforeMaximumAttempts() {
        PipelineTaskMapper mapper = mock(PipelineTaskMapper.class);
        PipelineTask task = new PipelineTask();
        task.setId(101L);
        task.setAttempt(1);
        task.setMaxAttempts(3);
        task.setStatus(PipelineTaskStatus.RUNNING.name());
        when(mapper.selectById(101L)).thenReturn(task);
        when(mapper.update(isNull(), any())).thenReturn(1);
        PipelineTaskManager manager = new PipelineTaskManager(mapper);

        manager.markFailed(101L, new IllegalStateException("model timeout"));

        ArgumentCaptor<LambdaUpdateWrapper<PipelineTask>> wrapperCaptor =
                ArgumentCaptor.forClass(LambdaUpdateWrapper.class);
        verify(mapper).update(isNull(), wrapperCaptor.capture());
        assertTrue(wrapperCaptor.getValue().getParamNameValuePairs()
                .containsValue(PipelineTaskStatus.RETRY_WAIT.name()));
    }

    @Test
    void markFailedUsesTerminalFailureAtMaximumAttempts() {
        PipelineTaskMapper mapper = mock(PipelineTaskMapper.class);
        PipelineTask task = new PipelineTask();
        task.setId(101L);
        task.setAttempt(3);
        task.setMaxAttempts(3);
        task.setStatus(PipelineTaskStatus.RUNNING.name());
        when(mapper.selectById(101L)).thenReturn(task);
        when(mapper.update(isNull(), any())).thenReturn(1);
        PipelineTaskManager manager = new PipelineTaskManager(mapper);

        manager.markFailed(101L, new RuntimeException("permanent failure"));

        ArgumentCaptor<LambdaUpdateWrapper<PipelineTask>> wrapperCaptor =
                ArgumentCaptor.forClass(LambdaUpdateWrapper.class);
        verify(mapper).update(isNull(), wrapperCaptor.capture());
        assertTrue(wrapperCaptor.getValue().getParamNameValuePairs()
                .containsValue(PipelineTaskStatus.FAILED.name()));
    }

    @Test
    void staleRunningTasksAreRequeued() {
        PipelineTaskMapper mapper = mock(PipelineTaskMapper.class);
        when(mapper.update(isNull(), any())).thenReturn(2);
        PipelineTaskManager manager = new PipelineTaskManager(mapper);

        int recovered = manager.recoverStaleRunningTasks(LocalDateTime.now().minusMinutes(10));

        assertEquals(2, recovered);
        ArgumentCaptor<LambdaUpdateWrapper<PipelineTask>> wrapperCaptor =
                ArgumentCaptor.forClass(LambdaUpdateWrapper.class);
        verify(mapper).update(isNull(), wrapperCaptor.capture());
        assertTrue(wrapperCaptor.getValue().getParamNameValuePairs()
                .containsValue(PipelineTaskStatus.RETRY_WAIT.name()));
    }

    @Test
    void failedTaskCanBeManuallyRequeued() {
        PipelineTaskMapper mapper = mock(PipelineTaskMapper.class);
        when(mapper.update(isNull(), any())).thenReturn(1, 0);
        PipelineTaskManager manager = new PipelineTaskManager(mapper);

        assertTrue(manager.requeueFailed(101L));
        assertFalse(manager.requeueFailed(102L));
        verify(mapper, org.mockito.Mockito.times(2)).update(isNull(), any());
    }
}
