package com.springtutorials.timeline.dummytimeline;

import com.springtutorials.timeline.batch.process.StateTasklet;
import com.springtutorials.timeline.common.service.hazelcast.AbstractHazelcastProcessingHelper;
import com.springtutorials.timeline.common.service.hazelcast.ProcessStepEntryStatus;
import com.springtutorials.timeline.exception.JobConfigurationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

import static com.springtutorials.timeline.config.batch.BatchJobConfiguration.REPORT_DATE_PARAM;

@Slf4j
@Component
@StepScope
public class DummyTimelineTasklet1 extends StateTasklet {
    private final AbstractHazelcastProcessingHelper<String> hazelcastHelper;

    public DummyTimelineTasklet1(
            @Qualifier("dummyTimelineHazelcastHelper1")
            AbstractHazelcastProcessingHelper<String> hazelcastHelper) {
        this.hazelcastHelper = hazelcastHelper;
    }

    @Override
    public RepeatStatus execute(StepContribution stepContribution, ChunkContext chunkContext) throws Exception {
        Object dummyIds = stepContribution.getStepExecution().getExecutionContext().get(DummyTimelinePartitioner1.DUMMY_TIMELINE_IDS_PARAM);
        var reportDateJobParam = stepContribution.getStepExecution().getJobParameters().getDate(REPORT_DATE_PARAM);

        if (reportDateJobParam != null && dummyIds instanceof List) {
            for (String dummyId : (List<String>) dummyIds) {
                Optional<String> recordForProcess = hazelcastHelper.getRecordForProcess(dummyId);
                if (recordForProcess.isPresent()) {
                    hazelcastHelper.setStatus(dummyId, ProcessStepEntryStatus.IN_PROCESS);
                    log.info("execute dummyId = {}", dummyId);
                    hazelcastHelper.setStatus(dummyId, ProcessStepEntryStatus.FINISHED);
                } else {
                    log.warn("Skipping id = {}", dummyId);
                }
            }
        } else {
            throw new JobConfigurationException(String.format("'%s' job param '%s' along with '%s' param '%s' from step " +
                            "execution context must be present", REPORT_DATE_PARAM, reportDateJobParam,
                    DummyTimelinePartitioner1.DUMMY_TIMELINE_IDS_PARAM, dummyIds));
        }
        return RepeatStatus.FINISHED;
    }

    @Override
    public void beforeStep(StepExecution stepExecution) {
        log.info("Starting creating payment refund registry creation step execution: {}", stepExecution);
    }
}
