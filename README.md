В аннотации @KafkaListener параметр batch используется для указания того, следует ли обрабатывать сообщения из Kafka в пакетном режиме (batch mode). Когда batch установлено в true, метод, аннотированный @KafkaListener, будет получать список сообщений вместо одного сообщения за раз. Это позволяет обрабатывать несколько сообщений одновременно, что может повысить производительность и эффективность обработки сообщений.

@KafkaListener(topics = "my-topic", groupId = "my-group", batch = "true")

Преимущества пакетной обработки (batch processing):
 При работе с транзакциями БД. При вызове внешних REST-сервисов

Настройка параметров пакетной обработки
Для того чтобы пакетная обработка работала эффективно, можно также настроить параметры потребителя в application.properties или application.yml. Например:

properties
Copy code
spring.kafka.consumer.enable-auto-commit=false
spring.kafka.consumer.max-poll-records=100
enable-auto-commit: Отключение автоматического подтверждения смещений позволяет контролировать подтверждение смещений вручную после обработки пакета сообщений.
max-poll-records: Устанавливает максимальное количество сообщений, которые потребитель может получить за один вызов.

Пример с ручным подтверждением смещений
Для полной настройки пакетной обработки часто используется ручное подтверждение смещений. Вот пример:

@Service
public class KafkaConsumerService {

    @KafkaListener(topics = "my-topic", groupId = "my-group", batch = "true")
    public void listen(List<ConsumerRecord<String, String>> records, Acknowledgment acknowledgment) {
        for (ConsumerRecord<String, String> record : records) {
            System.out.println("Received Message: " + record.value());
        }
        // Подтверждение после обработки пакета
        acknowledgment.acknowledge();
    }
}

По умолчанию параметр batch в аннотации @KafkaListener установлен в false. Это означает, что без явного указания batch = "true", метод, аннотированный @KafkaListener, будет получать одно сообщение за раз.

Помимо явного указания параметра batch в аннотации @KafkaListener, существуют и другие способы установить значение для пакетной обработки сообщений в Spring Kafka:

1. Настройка через ConcurrentKafkaListenerContainerFactory

@Configuration
@EnableKafka
public class KafkaConsumerConfig {

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, String> kafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, String> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory());

        // Включение пакетной обработки
        factory.setBatchListener(true);

        return factory;
    }

    @Bean
    public ConsumerFactory<String, String> consumerFactory() {
        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "my-group");
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        return new DefaultKafkaConsumerFactory<>(props);
    }
}

2. Установка через application.properties или application.yml
этот метод не включает пакетный режим сам по себе, но помогает контролировать количество сообщений, получаемых за один вызов.

   spring.kafka.consumer.max-poll-records=500

    spring:
        kafka:
            consumer:
                max-poll-records: 500


