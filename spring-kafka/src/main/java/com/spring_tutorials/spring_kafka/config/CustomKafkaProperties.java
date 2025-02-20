package com.spring_tutorials.spring_kafka.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component("customKafkaProperties")
public class CustomKafkaProperties {
    public static final Integer DEFAULT_TOPIC_REPLICA_COUNT = 1;
    public static final Integer DEFAULT_TOPIC_PARTITION_COUNT = 1;
    public static final Integer DEFAULT_CONCURRENCY = 1;
    public static final Long DEFAULT_BACKOFF_INTERVAL_MSEC = 1000L;
    public static final Long DEFAULT_MAX_RETRY_ATTEMPTS = Long.MAX_VALUE;
    public static final String DEFAULT_TOPIC = "registry-processing";

    private Integer topicPartitions = CustomKafkaProperties.DEFAULT_TOPIC_PARTITION_COUNT;
    private Integer topicReplicas = CustomKafkaProperties.DEFAULT_TOPIC_REPLICA_COUNT;
    private Integer concurrency = CustomKafkaProperties.DEFAULT_CONCURRENCY;
    private Long backOffInterval = CustomKafkaProperties.DEFAULT_BACKOFF_INTERVAL_MSEC;
    private Long maxRetryAttempts = CustomKafkaProperties.DEFAULT_MAX_RETRY_ATTEMPTS;
    private String topic = DEFAULT_TOPIC;
}
