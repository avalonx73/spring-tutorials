package com.springtutorials.spring_startup;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;

@Slf4j
@Configuration
public class CommandLineRunnerConfig {

    @Bean
    @Order(1)
    public CommandLineRunner CommandLineRunnerBean() {
        return (args) -> {
            log.info("CommandLineRunner bean running");

            for (String arg : args) {
                log.info(arg);
            }
        };
    }
}
