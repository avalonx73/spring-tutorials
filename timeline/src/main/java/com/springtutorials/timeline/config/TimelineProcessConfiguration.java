package com.springtutorials.timeline.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Configuration
@ConditionalOnProperty(name = "payment.executors.enabled", havingValue = "true")
public class TimelineProcessConfiguration {

    @Value("${payment.executors.nThreads}")
    private Integer nThreads;
    @Value("${payment.executors.scheduler.pool-size}")
    private Integer poolSize;
    @Value("${payment.executors.scheduler.thread-naming-prefix}")
    private String threadNamingPrefix;

    @Bean
    public ExecutorService timelineStepExecutorService() {
        return Executors.newFixedThreadPool(nThreads);
    }

    @Bean
    public ThreadPoolTaskScheduler updateTimelineProcessInfoExecutor() {
        ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
        scheduler.setPoolSize(poolSize);
        scheduler.setThreadNamePrefix(threadNamingPrefix);
        scheduler.setThreadPriority(Thread.MAX_PRIORITY);
        return scheduler;
    }
}
