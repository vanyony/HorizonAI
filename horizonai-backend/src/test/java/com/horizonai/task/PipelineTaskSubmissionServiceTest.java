package com.horizonai.task;

import com.horizonai.entity.PipelineTask;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class PipelineTaskSubmissionServiceTest {
    @Test
    void pendingTaskAndOutboxAreCreatedThroughOneServiceBoundary() {
        PipelineTaskManager taskManager = mock(PipelineTaskManager.class);
        PipelineOutboxManager outboxManager = mock(PipelineOutboxManager.class);
        PipelineTask task = new PipelineTask();
        task.setId(42L);
        task.setStatus(PipelineTaskStatus.PENDING.name());
        when(taskManager.create(org.mockito.ArgumentMatchers.anyString(),
                org.mockito.ArgumentMatchers.eq(PipelineTaskType.ANALYZE_ARTICLE),
                org.mockito.ArgumentMatchers.eq("9"),
                org.mockito.ArgumentMatchers.eq("analysis-9"),
                org.mockito.ArgumentMatchers.eq("{}"),
                org.mockito.ArgumentMatchers.eq(3))).thenReturn(task);

        PipelineTaskSubmissionService service = new PipelineTaskSubmissionService(taskManager, outboxManager);
        PipelineTask result = service.submit(PipelineTaskType.ANALYZE_ARTICLE, "9", "analysis-9", "{}");

        assertSame(task, result);
        verify(outboxManager).enqueue(42L);
    }

    @Test
    void completedIdempotentTaskIsNotRepublished() {
        PipelineTaskManager taskManager = mock(PipelineTaskManager.class);
        PipelineOutboxManager outboxManager = mock(PipelineOutboxManager.class);
        PipelineTask task = new PipelineTask();
        task.setId(42L);
        task.setStatus(PipelineTaskStatus.SUCCEEDED.name());
        when(taskManager.create(org.mockito.ArgumentMatchers.anyString(),
                org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any(),
                org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any(),
                org.mockito.ArgumentMatchers.anyInt())).thenReturn(task);

        new PipelineTaskSubmissionService(taskManager, outboxManager)
                .submit(PipelineTaskType.ANALYZE_ARTICLE, "9", "analysis-9", "{}");

        org.mockito.Mockito.verifyNoInteractions(outboxManager);
    }
}
