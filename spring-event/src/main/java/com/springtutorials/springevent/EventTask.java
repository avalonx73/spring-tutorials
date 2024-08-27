package com.springtutorials.springevent;

import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class EventTask {
    @EventListener
    public void handleCustomEvent(String event) {
        System.out.println("Event received: ");
    }
}
