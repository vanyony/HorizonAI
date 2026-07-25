package com.horizonai.task;

import com.horizonai.entity.PipelineTask;

public interface PipelineTaskHandler {

    PipelineTaskType taskType();

    void handle(PipelineTask task);
}
