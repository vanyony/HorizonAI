package com.horizonai.task;

import com.horizonai.collector.DemoContentCollector;
import com.horizonai.entity.PipelineTask;
import com.horizonai.service.ContentCollectionService;
import org.springframework.stereotype.Component;

@Component
public class DemoSyncTaskHandler implements PipelineTaskHandler {

    private final ContentCollectionService collectionService;

    public DemoSyncTaskHandler(ContentCollectionService collectionService) {
        this.collectionService = collectionService;
    }

    @Override
    public PipelineTaskType taskType() {
        return PipelineTaskType.COLLECT_DEMO;
    }

    @Override
    public void handle(PipelineTask task) {
        collectionService.collect(DemoContentCollector.SOURCE_NAME);
    }
}
