package com.springtutorials.timeline.dummytimeline;

import com.springtutorials.timeline.common.model.process.ProcessTimelineStepDefinition;
import com.springtutorials.timeline.common.service.hazelcast.AbstractHazelcastProcessingHelper;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.integration.partition.RemotePartitioningManagerStepBuilderFactory;
import org.springframework.batch.integration.partition.RemotePartitioningWorkerStepBuilderFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.integration.core.MessagingTemplate;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.IntegrationFlows;
import org.springframework.integration.kafka.dsl.Kafka;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.KafkaAdmin;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.messaging.MessageChannel;

import javax.annotation.PostConstruct;
import java.util.Properties;

@Configuration
@RequiredArgsConstructor
public class DummyTimelineBatchJobConfig1 {
    public static final String DUMMY_TIMELINE_STEP1_JOB
            = "dummy-timeline-step1-job";

    private static final String DUMMY_TIMELINE_MANAGER_STEP
            = "dummyTimelineManager";

    private static final String DUMMY_TIMELINE_WORKER_STEP =
            "dummyTimelineWorker";

    private static final String DUMMY_TIMELINE_WORKERS_GROUP_ID
            = "dummy-timeline-workers";

    private static final String DUMMY_TIMELINE_PARTITIONER_BEAN
            = "dummyTimelinePartitionerBean";

    private final JobBuilderFactory jobBuilderFactory;
    private final RemotePartitioningManagerStepBuilderFactory managerStepBuilderFactory;
    private final RemotePartitioningWorkerStepBuilderFactory workerStepBuilderFactory;
    private final DummyProperties dummyProperties;
    private final KafkaAdmin kafkaAdmin;

    @Bean
    public Job dummyTimelineJob(
            @Qualifier(DUMMY_TIMELINE_PARTITIONER_BEAN) DummyTimelinePartitioner1 partitioner) {
        var messagingTemplate = new MessagingTemplate();
        messagingTemplate.setDefaultChannel(dummyTimelinePartitionRequestChannel());

        return jobBuilderFactory.get(DUMMY_TIMELINE_STEP1_JOB)
                .start(managerStepBuilderFactory.get(DUMMY_TIMELINE_MANAGER_STEP)
                        .partitioner(DUMMY_TIMELINE_WORKER_STEP, partitioner)
                        .gridSize(10)
                        .messagingTemplate(messagingTemplate)
                        .allowStartIfComplete(true)
                        .build())
                .incrementer(new RunIdIncrementer())
                .build();
    }

    @Bean(name = DUMMY_TIMELINE_PARTITIONER_BEAN)
    public DummyTimelinePartitioner1 dummyTimelinePartitioner(@Qualifier("dummyTimelineHazelcastHelper1")
                                                              AbstractHazelcastProcessingHelper<String> hazelcastHelper) {
        return new DummyTimelinePartitioner1(hazelcastHelper,
                ProcessTimelineStepDefinition.DUMMY_TIMELINE_STEP1,
                "dummy_timeline_step1_ids_");
    }

    @Bean(name = DUMMY_TIMELINE_WORKER_STEP)
    public Step dummyTimelineWorker(DummyTimelineTasklet1 dummyTimelineTasklet) {
        return this.workerStepBuilderFactory.get(DUMMY_TIMELINE_WORKER_STEP)
                .inputChannel(inboundDummyTimelinePartitionChannel())
                .tasklet(dummyTimelineTasklet)
                .allowStartIfComplete(true)
                .build();
    }

    @Bean
    public MessageChannel dummyTimelinePartitionRequestChannel() {
        return new DirectChannel();
    }

    @Bean
    public IntegrationFlow dummyTimelineManagerPartitionFlow(KafkaTemplate<Object, Object> kafkaTemplate) {
        String topic = dummyProperties.getKafka().getBatch().getDummyStep1().getTopic();
        return IntegrationFlows.from(dummyTimelinePartitionRequestChannel())
                .handle(Kafka.outboundChannelAdapter(kafkaTemplate).topic(topic))
                .get();
    }

    @Bean
    public DirectChannel inboundDummyTimelinePartitionChannel() {
        return new DirectChannel();
    }

    @Bean
    public IntegrationFlow inboundDummyTimelinePartitionFlow(ConsumerFactory<Object, Object> consumerFactory) {
        String topic = dummyProperties.getKafka().getBatch().getDummyStep1().getTopic();
        var containerProps = new ContainerProperties(topic);
        containerProps.setKafkaConsumerProperties(getDummyTimelineKafkaProperties());
        var kafkaConsumerProperties = new Properties();

        containerProps.setKafkaConsumerProperties(kafkaConsumerProperties);
        containerProps.setGroupId(DUMMY_TIMELINE_WORKERS_GROUP_ID);
        return IntegrationFlows
                .from(Kafka.messageDrivenChannelAdapter(consumerFactory, containerProps))
                .channel(inboundDummyTimelinePartitionChannel())
                .get();
    }

    private Properties getDummyTimelineKafkaProperties() {
        return dummyProperties
                .getKafka()
                .getBatch()
                .getDummyStep1()
                .getProperties();
    }

    @PostConstruct
    public void dummyTimelineWorkerTopic() {
        String topic = dummyProperties.getKafka().getBatch().getDummyStep1().getTopic();
        Integer topicPartitions = dummyProperties.getKafka().getBatch().getDummyStep1()
                .getTopicPartitions();
        Integer topicReplicas = dummyProperties.getKafka().getBatch().getDummyStep1()
                .getTopicReplicas();
        kafkaAdmin.createOrModifyTopics(TopicBuilder.name(topic)
                .partitions(topicPartitions)
                .replicas(topicReplicas)
                .build());
    }

}
