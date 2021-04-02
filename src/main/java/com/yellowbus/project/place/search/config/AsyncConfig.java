package com.yellowbus.project.place.search.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@EnableAsync
@Configuration
public class AsyncConfig implements AsyncConfigurer {

    @Bean(name="threadPoolTakExecutor")
    public AsyncTaskExecutor threadPoolTakExecutor() {
        ThreadPoolTaskExecutor taskExecutor = new ThreadPoolTaskExecutor();
        taskExecutor.setCorePoolSize(5);
        taskExecutor.setMaxPoolSize(100);
        // max thread 가 동작할 때 대기할 수 있는 큐 사이즈
        // thread 와 queue 가 꽉차면 Exception 발생
        taskExecutor.setQueueCapacity(100);
        taskExecutor.setThreadNamePrefix("Concurrent Executor-");
        taskExecutor.initialize();

        return taskExecutor;
    }

}
