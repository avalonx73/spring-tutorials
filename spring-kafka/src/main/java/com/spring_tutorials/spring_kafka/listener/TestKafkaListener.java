package com.spring_tutorials.spring_kafka.listener;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

@Slf4j
@RequiredArgsConstructor
@Component("kafkaListener")
public class TestKafkaListener {
    @KafkaListener(id = "registry",
            groupId = "registry-group",
            topics = "#{@customKafkaProperties.topic}",
            concurrency = "#{@customKafkaProperties.concurrency}",
            containerFactory = "kafkaListenerContainerFactory")
    public void listen(ConsumerRecord<?, ?> record, Acknowledgment acknowledgment) {
        log.info("topic: {}, partition: {}, offset: {}", record.topic(), record.partition(), record.offset());
        acknowledgment.acknowledge();
    }
}
