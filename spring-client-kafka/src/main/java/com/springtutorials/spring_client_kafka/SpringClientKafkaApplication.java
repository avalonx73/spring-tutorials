package com.springtutorials.spring_client_kafka;

import com.springtutorials.spring_client_kafka.service.ConsumerService;
import com.springtutorials.spring_client_kafka.service.ProducerService;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.common.errors.TopicExistsException;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;

import java.util.Collections;
import java.util.Properties;

import static com.springtutorials.spring_client_kafka.utils.Utils.sleepMiliseconds;
import static com.springtutorials.spring_client_kafka.utils.Utils.sleepMinutes;


@Slf4j
@SpringBootApplication
public class SpringClientKafkaApplication {

    private final static String TOPIC = "spring-kafka-demo";

    @SneakyThrows
    public static void main(String[] args) {

        ApplicationContext context = SpringApplication.run(SpringClientKafkaApplication.class, args);

        createTopic(TOPIC, 3, (short) 1);

        ProducerService producerService = context.getBean(ProducerService.class);

        new Thread(() -> {
            for (int i = 1; i <= 200; i++) {
                producerService.send("key" + i, "message" + i, true);
                sleepMiliseconds(100);
            }
        }).start();

        ConsumerService consumerService = context.getBean(ConsumerService.class);

        consumerService.consume(r -> System.out.println(r.key() + " value: " + r.value()));

        log.info("Wait...");
        sleepMinutes(5);
        consumerService.setRunning(false);
        sleepMinutes(1);
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
