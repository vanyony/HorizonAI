package com.horizonai.task;

import com.horizonai.entity.PipelineTask;
import com.horizonai.mapper.PipelineTaskMapper;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class PipelineTaskManagerTest {

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
        assertEquals(PipelineTaskStatus.PENDING.name(), task.getStatus());
        assertEquals(0, task.getAttempt());
        assertEquals(3, task.getMaxAttempts());
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
}
