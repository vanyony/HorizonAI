package com.horizonai.event;

import com.horizonai.task.PipelineTaskDispatcher;
import com.horizonai.task.PipelineTaskType;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
public class ContentAnalysisTaskListener {

    private final PipelineTaskDispatcher taskDispatcher;
    private final String promptVersion;

    public ContentAnalysisTaskListener(
            PipelineTaskDispatcher taskDispatcher,
            @Value("${ai.analysis.prompt-version:v1}") String promptVersion) {
        this.taskDispatcher = taskDispatcher;
        this.promptVersion = promptVersion;
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onContentIngested(ContentIngestedEvent event) {
        String bizKey = String.valueOf(event.getArticleId());
        String idempotencyKey = String.format(
                "article-analysis:%d:%s:%s",
                event.getArticleId(),
                event.getContentHash(),
                promptVersion
        );
        taskDispatcher.submit(
                PipelineTaskType.ANALYZE_ARTICLE,
                bizKey,
                idempotencyKey,
                "{}"
        );
    }
}
