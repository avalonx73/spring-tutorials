package com.springtutorials.spring_async.configuration;

import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;

@Configuration
public class WebConfiguration {
    /**
     * Использование RestTemplateBuilder: позволяет гибко настраивать клиент,
     * добавляя необходимые параметры (например, таймауты, интерцепторы, конфигурации сериализации и десериализации)
     */
    @Bean("httpClient")
    public RestTemplate httpClient(RestTemplateBuilder builder) throws Exception {
        return builder
                .setReadTimeout(Duration.ofSeconds(90))
                .setConnectTimeout(Duration.ofSeconds(90))
                .build();
    }
}
