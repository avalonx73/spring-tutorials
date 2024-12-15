package com.spring_tutorials.spring_kafka.producer;

import com.spring_tutorials.spring_kafka.config.CustomKafkaProperties;
import com.spring_tutorials.spring_kafka.dto.kafka.MessagePayload;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TestKafkaProducer {
    private final KafkaTemplate<String, MessagePayload> kafkaTemplate;
    private final CustomKafkaProperties customKafkaProperties;
    public void sendMessage(MessagePayload messagePayload) {
        String topic = customKafkaProperties.getTopic();
        kafkaTemplate.send(topic, messagePayload.getFileId(), messagePayload);
    }
}
