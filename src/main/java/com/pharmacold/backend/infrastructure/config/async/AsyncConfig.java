package com.pharmacold.backend.infrastructure.config.async;

import lombok.RequiredArgsConstructor;
import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

@RequiredArgsConstructor
@Configuration
@EnableAsync
public class AsyncConfig implements AsyncConfigurer {

    private final CustomAsyncExceptionHandler customAsyncExceptionHandler;

    @Bean("notificationTaskExecutor")
    public Executor notificationExecutor() {
        int cpuCores = Runtime.getRuntime().availableProcessors();

        ThreadPoolTaskExecutor poolExecutor = new ThreadPoolTaskExecutor();
        poolExecutor.setCorePoolSize(Math.max(2, cpuCores));
        poolExecutor.setMaxPoolSize(Math.max(4, cpuCores * 4));
        poolExecutor.setQueueCapacity(250);
        poolExecutor.setThreadNamePrefix("notif-worker-");

        poolExecutor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());

        poolExecutor.setWaitForTasksToCompleteOnShutdown(true);
        poolExecutor.setAwaitTerminationSeconds(30);
        poolExecutor.initialize();

        return poolExecutor;
    }

    @Override
    public AsyncUncaughtExceptionHandler getAsyncUncaughtExceptionHandler() {
        return customAsyncExceptionHandler;
    }
}
