package com.springtutorials.timeline.dummytimeline;

import com.springtutorials.timeline.common.model.process.ProcessTimelineStepDefinition;
import com.springtutorials.timeline.common.service.hazelcast.AbstractHazelcastProcessingHelper;
import com.springtutorials.timeline.common.service.hazelcast.HazelcastProcessStepInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.partition.support.Partitioner;
import org.springframework.batch.item.ExecutionContext;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static com.springtutorials.timeline.common.model.process.ProcessTimelineStepDefinition.DUMMY_TIMELINE_STEP1;

@Slf4j
public class DummyTimelinePartitioner1 implements Partitioner {
    static final String DUMMY_TIMELINE_IDS_PARAM = "dummyTimelineIds";
    private final AbstractHazelcastProcessingHelper<String> hazelcastHelper;
    private final ProcessTimelineStepDefinition stepDefinition;
    private final String partitionPrefix;

    public DummyTimelinePartitioner1(
            AbstractHazelcastProcessingHelper<String> hazelcastHelper,
            ProcessTimelineStepDefinition stepDefinition,
            String partitionPrefix) {
        this.hazelcastHelper = hazelcastHelper;
        this.stepDefinition = stepDefinition;
        this.partitionPrefix = partitionPrefix;
    }

    @Override
    public Map<String, ExecutionContext> partition(int chunkSize) {
        HazelcastProcessStepInfo processStepInfo = hazelcastHelper.getProcessStepInfoMap().get(DUMMY_TIMELINE_STEP1.name());
        if (processStepInfo != null) {
            var reportDate = LocalDate.from(DateTimeFormatter.ISO_DATE.parse(processStepInfo.getReportDate()));
        }

        List<String> listIds = IntStream.rangeClosed(1, 50)
                .mapToObj(String::valueOf)
                .collect(Collectors.toList());

        var executionContexts = partitionDummyIds(listIds, chunkSize)
                .stream()
                .map(DummyTimelinePartitioner1::createExecutionContext)
                .collect(Collectors.toList());

        Map<String, ExecutionContext> executionContextMap = new HashMap<>();

        for (var i = 0; i < executionContexts.size(); i++) {
            executionContextMap.put(createPartitionKey(i), executionContexts.get(i));
        }

        hazelcastHelper.clearMapAndPutRecords(listIds);
        // processStepInfo.setRecordsForProceed(listIds.size());
        //  hazelcastHelper.getProcessStepInfoMap().put(stepDefinition.name(), processStepInfo);

        for (String listId : listIds) {
            Optional<String> recordForProcess = hazelcastHelper.getRecordForProcess(listId);
            if (recordForProcess.isPresent()) {
                String record = recordForProcess.get();
                log.info(record);
            }
        }

        return executionContextMap;
    }

    private Collection<List<String>> partitionDummyIds(List<String> paymentIds, int partitionSize) {
        return IntStream.range(0, paymentIds.size())
                .boxed()
                .collect(Collectors.groupingBy(partition -> (partition / partitionSize),
                        Collectors.mapping(paymentIds::get,
                                Collectors.toList())))
                .values();
    }

    private String createPartitionKey(int i) {
        return partitionPrefix + i;
    }

    private static ExecutionContext createExecutionContext(List<String> providerPaymentIds) {
        var context = new ExecutionContext();
        context.put(DUMMY_TIMELINE_IDS_PARAM, providerPaymentIds);
        return context;
    }
}

