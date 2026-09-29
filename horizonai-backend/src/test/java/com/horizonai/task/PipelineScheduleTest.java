package com.horizonai.task;

import org.junit.jupiter.api.Test;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

class PipelineScheduleTest {

    @Test
    void configuredRssAndGitHubAreBothScheduled() {
        PipelineTaskDispatcher dispatcher = mock(PipelineTaskDispatcher.class);
        PipelineSchedule schedule = new PipelineSchedule(dispatcher, "https://example.com/feed.xml");

        schedule.scheduleContentCollection();

        verify(dispatcher).submit(eq(PipelineTaskType.COLLECT_GITHUB), anyString(), anyString(), eq("{}"));
        verify(dispatcher).submit(eq(PipelineTaskType.COLLECT_RSS), anyString(), anyString(), eq("{}"));
    }

    @Test
    void missingRssConfigurationDoesNotCreateFailingTask() {
        PipelineTaskDispatcher dispatcher = mock(PipelineTaskDispatcher.class);
        PipelineSchedule schedule = new PipelineSchedule(dispatcher, "");

        schedule.scheduleContentCollection();

        verify(dispatcher).submit(eq(PipelineTaskType.COLLECT_GITHUB), anyString(), anyString(), eq("{}"));
        verify(dispatcher, never()).submit(eq(PipelineTaskType.COLLECT_RSS), anyString(), anyString(), anyString());
    }
}
