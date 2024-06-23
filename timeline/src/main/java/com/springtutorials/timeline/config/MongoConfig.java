package com.springtutorials.timeline.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

@Configuration
@EnableMongoRepositories(basePackages = "com.springtutorials.timeline.repository.mongo")
public class MongoConfig {
}

