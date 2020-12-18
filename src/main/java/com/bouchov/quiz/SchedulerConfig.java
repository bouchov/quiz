package com.bouchov.quiz;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

@Configuration
public class SchedulerConfig {
    private final Logger logger = LoggerFactory.getLogger(SchedulerConfig.class);

    @Value("${thread.pool.size}")
    private int POOL_SIZE;

    @Bean
    public ThreadPoolTaskScheduler quizScheduler(){
        ThreadPoolTaskScheduler threadPoolTaskScheduler
                = new ThreadPoolTaskScheduler();
        int poolSize = POOL_SIZE == 0 ? 3 : POOL_SIZE;
        threadPoolTaskScheduler.setPoolSize(poolSize);
        String namePrefix = "QuizScheduler-";
        threadPoolTaskScheduler.setThreadNamePrefix(namePrefix);
        logger.debug("Starting scheduler {} with {} threads", namePrefix, poolSize);
        return threadPoolTaskScheduler;
    }
}
