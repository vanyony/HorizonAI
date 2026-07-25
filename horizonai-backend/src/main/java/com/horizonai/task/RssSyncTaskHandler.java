package com.horizonai.task;

import com.horizonai.collector.RssContentCollector;
import com.horizonai.entity.PipelineTask;
import com.horizonai.service.ContentCollectionService;
import org.springframework.stereotype.Component;

@Component
public class RssSyncTaskHandler implements PipelineTaskHandler {

    private final ContentCollectionService collectionService;

    public RssSyncTaskHandler(ContentCollectionService collectionService) {
        this.collectionService = collectionService;
    }

    @Override
    public PipelineTaskType taskType() {
        return PipelineTaskType.COLLECT_RSS;
    }

    @Override
    public void handle(PipelineTask task) {
        collectionService.collect(RssContentCollector.SOURCE_NAME);
    }
}
