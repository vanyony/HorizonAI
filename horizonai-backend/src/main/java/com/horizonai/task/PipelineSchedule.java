package com.horizonai.task;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
public class PipelineSchedule {

    private static final Logger log = LoggerFactory.getLogger(PipelineSchedule.class);

    private final PipelineTaskDispatcher taskDispatcher;

    public PipelineSchedule(PipelineTaskDispatcher taskDispatcher) {
        this.taskDispatcher = taskDispatcher;
    }

    @Scheduled(cron = "${pipeline.github-sync-cron:0 0 2 * * ?}")
    public void scheduleDailyGitHubSync() {
        String date = LocalDate.now().toString();
        taskDispatcher.submit(
                PipelineTaskType.COLLECT_GITHUB,
                date,
                "github-sync:" + date,
                "{}"
        );
        log.info("已提交每日 GitHub 同步任务: date={}", date);
    }
}
