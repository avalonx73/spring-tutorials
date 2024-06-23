package com.springtutorials.timeline.common.config;

import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.kafka.DefaultKafkaConsumerFactoryCustomizer;
import org.springframework.boot.autoconfigure.kafka.DefaultKafkaProducerFactoryCustomizer;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaAdmin;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.support.LoggingProducerListener;
import org.springframework.kafka.support.ProducerListener;
import org.springframework.kafka.support.converter.RecordMessageConverter;
import org.springframework.kafka.support.converter.StringJsonMessageConverter;

import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableKafka
public class CommonKafkaConfiguration {
    private final KafkaProperties kafkaProperties;
    private final RecordMessageConverter messageConverter;
    private final KafkaTemplate<Object, Object> kafkaTemplate;

    public CommonKafkaConfiguration(KafkaProperties properties,
                                    ObjectProvider<RecordMessageConverter> messageConverter,
                                    KafkaTemplate<Object, Object> kafkaTemplate) {
        this.kafkaProperties = properties;
        this.messageConverter = messageConverter.getObject();
        this.kafkaTemplate = kafkaTemplate;
    }

    @Configuration
    @EnableConfigurationProperties(KafkaProperties.class)
    @RequiredArgsConstructor
    public static class TemplateConfig {
        private final KafkaProperties kafkaProperties;

        @Bean
        public KafkaTemplate<Object, Object> kafkaTemplate(ProducerFactory<Object, Object> kafkaProducerFactory,
                                                           ProducerListener<Object, Object> kafkaProducerListener,
                                                           RecordMessageConverter messageConverter) {
            KafkaTemplate<Object, Object> template = new KafkaTemplate<>(kafkaProducerFactory);
            template.setMessageConverter(messageConverter);
            template.setProducerListener(kafkaProducerListener);
            template.setDefaultTopic(kafkaProperties.getTemplate().getDefaultTopic());
            return template;
        }

        @Bean
        public RecordMessageConverter jsonMsgConverter() {
            return new StringJsonMessageConverter();
        }

        @Bean
        public ProducerListener<Object, Object> kafkaProducerListener() {
            return new LoggingProducerListener<>();
        }

        @Bean
        public ProducerFactory<Object, Object> kafkaProducerFactory(
                ObjectProvider<DefaultKafkaProducerFactoryCustomizer> customizers) {
            DefaultKafkaProducerFactory<Object, Object> factory = new DefaultKafkaProducerFactory<>(
                    this.kafkaProperties.buildProducerProperties());
            String transactionIdPrefix = this.kafkaProperties.getProducer().getTransactionIdPrefix();
            if (transactionIdPrefix != null) {
                factory.setTransactionIdPrefix(transactionIdPrefix);
            }
            customizers.orderedStream().forEach(customizer -> customizer.customize(factory));
            return factory;
        }
    }

    @Bean
    public ConsumerFactory<Object, Object> kafkaConsumerFactory(
            ObjectProvider<DefaultKafkaConsumerFactoryCustomizer> customizers) {
        DefaultKafkaConsumerFactory<Object, Object> factory = new DefaultKafkaConsumerFactory<>(
                this.kafkaProperties.buildConsumerProperties());
        customizers.orderedStream().forEach(customizer -> customizer.customize(factory));
        return factory;
    }

    @Bean
    ConcurrentKafkaListenerContainerFactory<Object, Object> kafkaListenerContainerFactory(
            ConsumerFactory<Object, Object> kafkaConsumerFactory) {
        ConcurrentKafkaListenerContainerFactory<Object, Object> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(kafkaConsumerFactory);
        configureContainer(factory);
        return factory;
    }

    private void configureContainer(ConcurrentKafkaListenerContainerFactory<Object, Object> factory) {
        factory.setMessageConverter(messageConverter);
        factory.setReplyTemplate(kafkaTemplate);
        KafkaConfigurationUtils.configureFactory(factory, kafkaProperties);
    }

    @Bean
    public KafkaAdmin kafkaAdmin() {
        Map<String, Object> configs = new HashMap<>();
        configs.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaProperties.getBootstrapServers());
        return new KafkaAdmin(configs);
    }
}

