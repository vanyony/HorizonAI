package com.horizonai.task;

import com.horizonai.entity.PipelineOutboxEvent;
import com.horizonai.messaging.PipelineMessagePublisher;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class PipelineOutboxRelayTest {
    @Test
    void successfulPublishMarksEventPublished() {
        PipelineOutboxManager manager = mock(PipelineOutboxManager.class);
        PipelineMessagePublisher publisher = mock(PipelineMessagePublisher.class);
        PipelineOutboxEvent event = event(1L, 99L);
        when(manager.findReady(10)).thenReturn(List.of(event));
        when(manager.claim(1L)).thenReturn(true);

        new PipelineOutboxRelay(manager, publisher, 10).relay();

        verify(publisher).publish(99L);
        verify(manager).markPublished(1L);
    }

    @Test
    void publishFailureReturnsEventToOutboxRetry() {
        PipelineOutboxManager manager = mock(PipelineOutboxManager.class);
        PipelineMessagePublisher publisher = mock(PipelineMessagePublisher.class);
        PipelineOutboxEvent event = event(1L, 99L);
        RuntimeException failure = new RuntimeException("broker unavailable");
        when(manager.findReady(10)).thenReturn(List.of(event));
        when(manager.claim(1L)).thenReturn(true);
        org.mockito.Mockito.doThrow(failure).when(publisher).publish(99L);

        new PipelineOutboxRelay(manager, publisher, 10).relay();

        verify(manager).markPublishFailed(1L, failure);
    }

    private PipelineOutboxEvent event(Long id, Long taskId) {
        PipelineOutboxEvent event = new PipelineOutboxEvent();
        event.setId(id);
        event.setTaskId(taskId);
        return event;
    }
}
