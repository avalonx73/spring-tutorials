package com.springtutorials.spring_kafka;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.listener.KafkaMessageListenerContainer;
import org.springframework.kafka.listener.MessageListener;

import java.util.HashMap;
import java.util.Map;

public class KafkaListenerContainerExample {

    public static void main(String[] args) {
        // Настройки Kafka Consumer
        Map<String, Object> consumerProps = new HashMap<>();
        consumerProps.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        consumerProps.put(ConsumerConfig.GROUP_ID_CONFIG, "example-group");
        consumerProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        consumerProps.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);

        // Создаем фабрику для Consumer
        ConsumerFactory<String, String> consumerFactory = new DefaultKafkaConsumerFactory<>(consumerProps);

        // Настройки контейнера
        ContainerProperties containerProps = new ContainerProperties("example-topic");
        containerProps.setGroupId("example-group");

        // Устанавливаем MessageListener для обработки сообщений
        containerProps.setMessageListener((MessageListener<String, String>) record -> {
            System.out.printf("Consumed message: key=%s, value=%s, topic=%s, partition=%d, offset=%d%n",
                    record.key(), record.value(), record.topic(), record.partition(), record.offset());
        });

        // Создаем контейнер KafkaMessageListenerContainer
        KafkaMessageListenerContainer<String, String> container =
                new KafkaMessageListenerContainer<>(consumerFactory, containerProps);

        // Запускаем контейнер
        container.start();

        // Остановка контейнера при завершении приложения
        Runtime.getRuntime().addShutdownHook(new Thread(container::stop));
    }
}