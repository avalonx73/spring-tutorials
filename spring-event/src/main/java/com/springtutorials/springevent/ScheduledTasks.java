package com.springtutorials.springevent;

import org.springframework.stereotype.Component;

@Component
public class ScheduledTasks {

  //  @Scheduled(fixedRate = 5000)  // Задача будет выполняться каждые 5000 миллисекунд (5 секунд)
    public void reportCurrentTime() {
        System.out.println("The time is now " + System.currentTimeMillis());
    }
}