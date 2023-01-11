package com.bouchov.quiz.services;

import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.stereotype.Service;

@Service
public class ScheduledTaskService {
    private final ThreadPoolTaskScheduler quizScheduler;

    @Autowired
    public ScheduledTaskService(ThreadPoolTaskScheduler quizScheduler) {
        this.quizScheduler = quizScheduler;
    }


    @Transactional
    public void transactional(Runnable task) {
        task.run();
    }
}
