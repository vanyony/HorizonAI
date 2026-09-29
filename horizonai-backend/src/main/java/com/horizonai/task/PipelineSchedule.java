package com.horizonai.task;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Component
public class PipelineSchedule {

    private static final Logger log = LoggerFactory.getLogger(PipelineSchedule.class);
    private static final DateTimeFormatter WINDOW_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd-HH");

    private final PipelineTaskDispatcher taskDispatcher;
    private final String rssUrls;

    public PipelineSchedule(PipelineTaskDispatcher taskDispatcher,
                            @Value("${collector.rss.urls:}") String rssUrls) {
        this.taskDispatcher = taskDispatcher;
        this.rssUrls = rssUrls;
    }

    @Scheduled(cron = "${pipeline.content-sync-cron:0 0 */2 * * ?}")
    public void scheduleContentCollection() {
        String window = LocalDateTime.now().format(WINDOW_FORMAT);
        taskDispatcher.submit(
                PipelineTaskType.COLLECT_GITHUB,
                window,
                "github-sync:" + window,
                "{}"
        );
        log.info("已提交 GitHub 内容采集任务: window={}", window);

        if (StringUtils.hasText(rssUrls)) {
            taskDispatcher.submit(
                    PipelineTaskType.COLLECT_RSS,
                    window,
                    "rss-sync:" + window,
                    "{}"
            );
            log.info("已提交 RSS 内容采集任务: window={}", window);
        }
    }
}
