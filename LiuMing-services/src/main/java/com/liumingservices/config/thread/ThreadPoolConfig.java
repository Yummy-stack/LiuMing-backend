package com.liumingservices.config.thread;

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

    @Bean(name = "syncEquVectorPool")
    public ThreadPoolTaskExecutor threadPoolTaskExecutor() {
        ThreadPoolTaskExecutor syncEquVectorPool = new ThreadPoolTaskExecutor();
        syncEquVectorPool.setCorePoolSize(10);
        syncEquVectorPool.setMaxPoolSize(10);
        syncEquVectorPool.setKeepAliveSeconds(60);
        syncEquVectorPool.setQueueCapacity(10);
        syncEquVectorPool.setRejectedExecutionHandler(new ThreadPoolExecutor.DiscardPolicy());
        syncEquVectorPool.setThreadNamePrefix("syncEquVectorPool");
        syncEquVectorPool.initialize();

        return syncEquVectorPool;
    }

}
