package com.horizonai.task;

import com.horizonai.entity.PipelineTask;
import com.horizonai.collector.GitHubContentCollector;
import com.horizonai.service.ContentCollectionService;
import org.springframework.stereotype.Component;

@Component
public class GitHubSyncTaskHandler implements PipelineTaskHandler {

    private final ContentCollectionService collectionService;

    public GitHubSyncTaskHandler(ContentCollectionService collectionService) {
        this.collectionService = collectionService;
    }

    @Override
    public PipelineTaskType taskType() {
        return PipelineTaskType.COLLECT_GITHUB;
    }

    @Override
    public void handle(PipelineTask task) {
        collectionService.collect(GitHubContentCollector.SOURCE_NAME);
    }
}
