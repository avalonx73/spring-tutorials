package com.springtutorials.spring_kafka.kafka_clients;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.common.errors.TopicExistsException;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.Collections;
import java.util.Properties;

import static com.springtutorials.spring_kafka.utils.Utils.sleepMiliseconds;
import static com.springtutorials.spring_kafka.utils.Utils.sleepMinutes;


@Slf4j
@SpringBootApplication
public class SpringKafkaClientsApplication {

    private final static String TOPIC = "spring-kafka-demo";

    @SneakyThrows
    public static void main(String[] args) {

        SpringApplication.run(SpringKafkaClientsApplication.class, args);

        createTopic(TOPIC, 3, (short) 1);

        TestProducer testProducer = new TestProducer(TOPIC);

        new Thread(() -> {
            for (int i = 1; i <= 200; i++) {
                testProducer.send("key" + i, "message" + i, true);
                sleepMiliseconds(100);
            }
        }).start();

        TestConsumer testConsumer = new TestConsumer(TOPIC);

        testConsumer.consume(r -> System.out.println(r.key() + " value: " + r.value()));

        log.info("Wait...");
        sleepMinutes(5);
        testConsumer.setRunning(false);
        sleepMinutes(1);
        testProducer.close();
        testConsumer.close();
    }

    private static void createTopic(String topic, int numPartitions, short replicationFactor)  {
        Properties props = new Properties();
        props.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        AdminClient adminClient = AdminClient.create(props);
        NewTopic newTopic = new NewTopic(topic, numPartitions, replicationFactor); // 3 партиции, 1 репликация
        try {
            adminClient.createTopics(Collections.singleton(newTopic)).all().get();
        } catch (Exception e) {
            if (e.getCause() instanceof TopicExistsException) {
                log.warn("Топик уже существует.");
            } else {
                throw new RuntimeException(e);
            }
        }

        log.info("Топик успешно создан.");
    }

}
