package com.springtutorials.spring_startup;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationContextInitializedEvent;
import org.springframework.boot.context.event.ApplicationEnvironmentPreparedEvent;
import org.springframework.boot.context.event.ApplicationFailedEvent;
import org.springframework.boot.context.event.ApplicationPreparedEvent;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.boot.context.event.ApplicationStartingEvent;
import org.springframework.boot.context.event.SpringApplicationEvent;
import org.springframework.context.event.ContextStartedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class RunAfterStartup {
    @EventListener(ApplicationReadyEvent.class)
    public void runAfterApplicationReadyEvent() {
        log.info("ApplicationReadyEvent"); // Start
    }
    @EventListener(ApplicationContextInitializedEvent.class)
    public void runAfterApplicationContextInitializedEvent() {
        log.info("ApplicationContextInitializedEvent");
    }
    @EventListener(ApplicationEnvironmentPreparedEvent.class)
    public void runAfterApplicationEnvironmentPreparedEvent() {
        log.info("ApplicationEnvironmentPreparedEvent");
    }
    @EventListener(ApplicationFailedEvent.class)
    public void runAfterApplicationFailedEvent() {
        log.info("ApplicationFailedEvent");
    }
    @EventListener(ApplicationPreparedEvent.class)
    public void runAfterApplicationPreparedEvent() {
        log.info("ApplicationPreparedEvent");
    }
    @EventListener(ApplicationStartedEvent.class)
    public void runAfterApplicationStartedEvent() {
        log.info("ApplicationStartedEvent"); // Start
    }
    @EventListener(ApplicationStartingEvent.class)
    public void runAfterApplicationStartingEvent() {
        log.info("ApplicationStartingEvent");
    }
    @EventListener(SpringApplicationEvent.class)
    public void runAfterSpringApplicationEvent() {
        log.info("SpringApplicationEvent"); // Start
    }
    @EventListener(ContextStartedEvent.class)
    public void runAfterContextStartedEvent() {
        log.info("ContextStartedEvent"); // Start
    }

}
