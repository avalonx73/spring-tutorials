package com.springtutorials.spring_kafka.kafka_clients;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.util.StopWatch;

import java.io.Closeable;
import java.time.Duration;
import java.util.List;
import java.util.Properties;
import java.util.function.Consumer;

@Slf4j
@Setter
public class TestConsumer implements Closeable {
    private volatile boolean isRunning = true;
    private final String topic;

    private KafkaConsumer<String, String> consumer;

    public TestConsumer(String topic) {
        this.topic = topic;
        consumer = getConsumer(topic);
    }

    public void consume(Consumer<ConsumerRecord<String, String>> recordConsumer) {
        new Thread(() -> {
            StopWatch stopWatch = new StopWatch();
            while (isRunning) {
                /*
                Блокируется на указанное время (в данном случае, 5 секунд) или до тех пор, пока не будут получены сообщения
                Если в течение этого времени не будет получено никаких сообщений, poll вернет пустую коллекцию ConsumerRecords.
                */

                log.info("Start consumer.poll");
                stopWatch.start();
                ConsumerRecords<String, String> records = consumer.poll(Duration.ofSeconds(5));
                stopWatch.stop();
                log.info("Got ConsumerRecords, count: {}, {}, {}",
                        records.count(),
                        stopWatch.getLastTaskInfo().getTimeMillis(),
                        stopWatch.getTotalTimeMillis());

                records.forEach(record -> recordConsumer.accept(record));
            }
        }).start();
        return;
    }

    private KafkaConsumer<String, String> getConsumer(String topic) {
        var props = new Properties();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "groupId");
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);

        KafkaConsumer<String, String> consumer = new KafkaConsumer<>(props);
        consumer.subscribe(List.of(topic));
        return consumer;
    }

    @Override
    public void close() {
        isRunning = false;
        consumer.close();
    }
}
