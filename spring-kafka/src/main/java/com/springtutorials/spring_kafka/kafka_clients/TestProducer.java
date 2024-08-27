package com.springtutorials.spring_kafka.kafka_clients;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.serialization.StringSerializer;

import java.io.Closeable;
import java.util.Properties;
import java.util.concurrent.Future;

@Slf4j
public class TestProducer implements Closeable {
    private final String topic;

    private KafkaProducer<String, String> producer;

    public TestProducer(String topic) {
        this.topic = topic;
        producer = getProducer();
    }

    @SneakyThrows
    public void send(String key, String value, boolean async) {
        if (async) {
            /**
             * Асинхронный вызов. Не блокирует выполнение потока, процесс завершения отправки выполняется в фоновом потоке.
             * Как только вызывается метод producer.send(new ProducerRecord(...)), сообщение добавляется во внутренний буфер продюсера.
             * Этот буфер является очередью, в которую попадают сообщения перед их отправкой в Kafka.
             * После того как сообщение помещено в буфер, KafkaProducer отправляет его в брокер на основе различных условий, таких как:
             * Заполнение буфера (batch.size) (по умолчанию 16384 байт): Когда накопленный размер сообщений достигает заданного значения,
             * продюсер отправляет их пакетно.
             * Время ожидания (linger.ms) (по умолчанию 0 ms): Если за указанное время (например, 10 мс) не накапливается достаточно сообщений,
             * продюсер все равно отправляет те, что есть, чтобы избежать задержек.
             * Специальный поток, управляемый KafkaProducer, берет сообщения из буфера и отправляет их на брокер.
             * Этот процесс полностью асинхронный и не блокирует основной поток приложения.
             * После того как сообщение отправлено, KafkaProducer ожидает подтверждение от брокера.
             * Это подтверждение приходит асинхронно и обрабатывается в callback, если он был передан при вызове send().
              */
            ProducerRecord producerRecord = new ProducerRecord(topic, key, value);
            Future<RecordMetadata> future = producer.send(producerRecord, (metadata, exception) -> {
                if (exception == null) {
                    // Успешная отправка
                   log.info("Message sent successfully to topic " + metadata.topic() +
                            " partition " + metadata.partition() +
                            " at offset " + metadata.offset());
                } else {
                    // Ошибка при отправке
                    log.error("Error while sending message: " + exception.getMessage());
                    exception.printStackTrace();
                }
            });
        } else {
            /**
             * Метод get() блокирует выполнение потока до тех пор, пока сообщение не будет отправлено и
             * подтверждение не будет получено от брокера или пока не произойдет ошибка
             */
            Object o = producer.send(new ProducerRecord(topic, key, value)).get();
        }
    }

    private KafkaProducer<String, String> getProducer() {
        var props = new Properties();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        props.put(ProducerConfig.CLIENT_ID_CONFIG, "clientId");
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        props.put(ProducerConfig.LINGER_MS_CONFIG, 5000);  // Задержка в миллисекундах
        props.put(ProducerConfig.BATCH_SIZE_CONFIG, 320);  // Размер пакета в байтах (например, 32 KB)

        return new KafkaProducer<>(props);
    }

    @Override
    public void close() {
        producer.close();
    }
}
