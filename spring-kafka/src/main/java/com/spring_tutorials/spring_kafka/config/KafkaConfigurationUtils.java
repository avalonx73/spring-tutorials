package com.spring_tutorials.spring_kafka.config;

import lombok.experimental.UtilityClass;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.boot.context.properties.PropertyMapper;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;

import java.time.Duration;

@UtilityClass
public class KafkaConfigurationUtils {
    public static void configureFactory(ConcurrentKafkaListenerContainerFactory<?, ?> factory,
                                 KafkaProperties kafkaProperties) {
        var propertyMapper = PropertyMapper.get().alwaysApplyingWhenNonNull();

        var containerProps = factory.getContainerProperties();
        var listenerProps = kafkaProperties.getListener();
        propertyMapper.from(listenerProps::getAckMode).to(containerProps::setAckMode);
        propertyMapper.from(listenerProps::getConcurrency).to(factory::setConcurrency);
        propertyMapper.from(listenerProps::getClientId).to(containerProps::setClientId);
        propertyMapper.from(listenerProps::getAckCount).to(containerProps::setAckCount);
        propertyMapper.from(listenerProps::getAckTime).as(Duration::toMillis).to(containerProps::setAckTime);
        propertyMapper.from(listenerProps::getPollTimeout).as(Duration::toMillis).to(containerProps::setPollTimeout);
        propertyMapper.from(listenerProps::getNoPollThreshold).to(containerProps::setNoPollThreshold);
        propertyMapper.from(listenerProps.getIdleBetweenPolls()).as(Duration::toMillis)
                .to(containerProps::setIdleBetweenPolls);
        propertyMapper.from(listenerProps::getIdleEventInterval).as(Duration::toMillis)
                .to(containerProps::setIdleEventInterval);
        propertyMapper.from(listenerProps::getMonitorInterval).as(Duration::getSeconds).as(Number::intValue)
                .to(containerProps::setMonitorInterval);
        propertyMapper.from(listenerProps::getLogContainerConfig).to(containerProps::setLogContainerConfig);
        propertyMapper.from(listenerProps::isMissingTopicsFatal).to(containerProps::setMissingTopicsFatal);
    }
}