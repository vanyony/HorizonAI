package com.horizonai.task;

import com.horizonai.entity.PipelineTask;
import com.horizonai.service.AiAnalysisService;
import org.springframework.stereotype.Component;

@Component
public class AnalyzeArticleTaskHandler implements PipelineTaskHandler {

    private final AiAnalysisService aiAnalysisService;

    public AnalyzeArticleTaskHandler(AiAnalysisService aiAnalysisService) {
        this.aiAnalysisService = aiAnalysisService;
    }

    @Override
    public PipelineTaskType taskType() {
        return PipelineTaskType.ANALYZE_ARTICLE;
    }

    @Override
    public void handle(PipelineTask task) {
        try {
            aiAnalysisService.analyzeArticle(Long.parseLong(task.getBizKey()));
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("分析任务缺少有效文章 ID: " + task.getBizKey(), e);
        }
    }
}
