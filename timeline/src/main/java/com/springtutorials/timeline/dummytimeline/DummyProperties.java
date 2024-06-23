package com.springtutorials.timeline.dummytimeline;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.Properties;

@Getter
@Setter
@ConfigurationProperties("payment")
@Component("dummyProperties")
public class DummyProperties {
    private final DummyKafkaProperties kafka = new DummyKafkaProperties();

    @Getter
    @Setter
    public static class DummyKafkaProperties {
        public static final Integer DEFAULT_TOPIC_REPLICA_COUNT = 1;
        public static final Integer DEFAULT_TOPIC_PARTITION_COUNT = 1;
        public static final Integer DEFAULT_CONCURRENCY = 1;

        private final DummyKafkaBatchProperties batch = new DummyKafkaBatchProperties();
    }

    @Getter
    @Setter
    public static class DummyKafkaBatchProperties {
        private final DummyKafkaTopicBatchProperties dummyStep1 =
                new DummyKafkaTopicBatchProperties("dummy-step-1");
    }

    @Getter
    @Setter
    public static class DummyKafkaTopicBatchProperties {
        private Integer topicPartitions = DummyKafkaProperties.DEFAULT_TOPIC_PARTITION_COUNT;
        private Integer topicReplicas = DummyKafkaProperties.DEFAULT_TOPIC_REPLICA_COUNT;
        private final Properties properties = new Properties();
        private String topic;

        public DummyKafkaTopicBatchProperties(String topic) {
            this.topic = topic;
        }
    }

}
