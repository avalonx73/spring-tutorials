package com.springtutorials.spring_client_kafka.service;

import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.springframework.stereotype.Service;
import org.springframework.util.StopWatch;

import java.time.Duration;
import java.util.function.Consumer;

@Slf4j
@Setter
@Service
@RequiredArgsConstructor
public class ConsumerService {
    private volatile boolean isRunning = true;
    private final KafkaConsumer<String, String> consumer;

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
}
