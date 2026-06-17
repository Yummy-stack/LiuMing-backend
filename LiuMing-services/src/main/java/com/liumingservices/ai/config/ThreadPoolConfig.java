package com.liumingservices.ai.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.ThreadPoolExecutor;

@Configuration
@Slf4j
public class ThreadPoolConfig {

    @Bean(name = "memoryThreadPool")
    public ThreadPoolTaskExecutor threadPoolExecutor() {
        ThreadPoolTaskExecutor memoryThreadPool = new ThreadPoolTaskExecutor();
        memoryThreadPool.setCorePoolSize(10);
        memoryThreadPool.setMaxPoolSize(10);
        memoryThreadPool.setKeepAliveSeconds(60);
        memoryThreadPool.setQueueCapacity(10);
        memoryThreadPool.setRejectedExecutionHandler(new ThreadPoolExecutor.DiscardPolicy());
        memoryThreadPool.setThreadNamePrefix("memoryThreadPool");
        memoryThreadPool.initialize();

        return memoryThreadPool;
    }

}
