package com.spring_tutorials.spring_kafka.config;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.KafkaAdmin;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.SeekToCurrentErrorHandler;
import org.springframework.util.backoff.FixedBackOff;

import javax.annotation.PostConstruct;

@Slf4j
@Data
@Configuration
@RequiredArgsConstructor
public class KafkaConfig {
    private final KafkaAdmin kafkaAdmin;
    private final KafkaTemplate<Object, Object> kafkaTemplate;
    private final CustomKafkaProperties customKafkaProperties;

    @Bean
    public SeekToCurrentErrorHandler registryProcessingErrorHandler() {
        return new SeekToCurrentErrorHandler(
                new DeadLetterPublishingRecoverer(kafkaTemplate),
                new FixedBackOff(
                        customKafkaProperties.getBackOffInterval(),
                        customKafkaProperties.getMaxRetryAttempts()));
    }

    @Bean
    ConcurrentKafkaListenerContainerFactory<Object, Object> kafkaListenerContainerFactory(
            ConsumerFactory<Object, Object> kafkaConsumerFactory,
            KafkaProperties kafkaProperties) {
        ConcurrentKafkaListenerContainerFactory<Object, Object> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(kafkaConsumerFactory);
        factory.setErrorHandler(registryProcessingErrorHandler());
        factory.setConcurrency(2);
        factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.MANUAL);
        KafkaConfigurationUtils.configureFactory(factory, kafkaProperties);
        return factory;
    }

    @PostConstruct
    public void createPaymentConfirmationsTopic() {
        log.info("INSTANCE_ID = " + System.getenv("INSTANCE_ID"));
        kafkaAdmin.createOrModifyTopics(
                TopicBuilder.name(customKafkaProperties.getTopic())
                        .partitions(customKafkaProperties.getTopicPartitions())
                        .replicas(customKafkaProperties.getTopicReplicas())
                        .build());
    }
}
