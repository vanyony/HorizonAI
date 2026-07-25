package com.horizonai.config;

import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskDecorator;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

@Configuration
public class AsyncTaskConfig {

    @Bean
    public TaskDecorator mdcTaskDecorator() {
        return runnable -> {
            Map<String, String> callerContext = MDC.getCopyOfContextMap();
            return () -> {
                Map<String, String> workerContext = MDC.getCopyOfContextMap();
                try {
                    if (callerContext == null) {
                        MDC.clear();
                    } else {
                        MDC.setContextMap(callerContext);
                    }
                    runnable.run();
                } finally {
                    if (workerContext == null) {
                        MDC.clear();
                    } else {
                        MDC.setContextMap(workerContext);
                    }
                }
            };
        };
    }

    @Bean(name = "pipelineThreadPoolExecutor")
    public Executor pipelineThreadPoolExecutor(
            TaskDecorator mdcTaskDecorator,
            @Value("${pipeline.executor.core-pool-size:2}") int corePoolSize,
            @Value("${pipeline.executor.max-pool-size:4}") int maxPoolSize,
            @Value("${pipeline.executor.queue-capacity:100}") int queueCapacity) {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setThreadNamePrefix("pipeline-");
        executor.setCorePoolSize(corePoolSize);
        executor.setMaxPoolSize(maxPoolSize);
        executor.setQueueCapacity(queueCapacity);
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(30);
        executor.setTaskDecorator(mdcTaskDecorator);
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.initialize();
        return executor;
    }
}
