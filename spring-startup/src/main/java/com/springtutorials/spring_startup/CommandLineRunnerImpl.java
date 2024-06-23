package com.springtutorials.spring_startup;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@Order(3)
public class CommandLineRunnerImpl implements CommandLineRunner {
    @Override
    public void run(String... args) {
        log.info("CommandLineRunner component running");
    }
}
