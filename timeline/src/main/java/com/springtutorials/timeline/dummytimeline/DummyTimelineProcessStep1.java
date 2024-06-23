package com.springtutorials.timeline.dummytimeline;

import com.springtutorials.timeline.common.model.process.ProcessTimeline;
import com.springtutorials.timeline.common.model.process.ProcessTimelineStepDefinition;
import com.springtutorials.timeline.common.model.process.TimelineStatus;
import com.springtutorials.timeline.common.service.hazelcast.AbstractHazelcastProcessingHelper;
import com.springtutorials.timeline.service.BatchJobFacade;
import com.springtutorials.timeline.service.process.AbstractBatchJobTimelineStepExecutorService;
import com.springtutorials.timeline.service.process.ProcessTimelineStepService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.JobParameter;
import org.springframework.batch.core.JobParameters;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZoneId;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static com.springtutorials.timeline.config.batch.BatchJobConfiguration.REPORT_DATE_PARAM;
import static com.springtutorials.timeline.config.batch.BatchJobConfiguration.TIMELINE_ID_PARAM;
import static com.springtutorials.timeline.dummytimeline.DummyTimelineBatchJobConfig1.DUMMY_TIMELINE_STEP1_JOB;

@Slf4j
@Service
public class DummyTimelineProcessStep1 extends AbstractBatchJobTimelineStepExecutorService<String> {

    public DummyTimelineProcessStep1(
            @Qualifier("dummyTimelineHazelcastHelper1")
            AbstractHazelcastProcessingHelper<String> hazelcastHelper,
            ProcessTimelineStepService processTimelineStepService,
            BatchJobFacade batchJobFacade) {
        super(hazelcastHelper, processTimelineStepService, batchJobFacade);
    }

    @Override
    @Transactional("mongoTransactionManager")
    public void rollBackStep(ProcessTimeline timeline) {}

    @Override
    public boolean isSpecialStepOperationsSupported() {
        return true;
    }

    @Override
    public ProcessTimelineStepDefinition getStepDefinition() {
        return ProcessTimelineStepDefinition.DUMMY_TIMELINE_STEP1;
    }

    @Override
    protected JobParameters createJobParameters(ProcessTimeline timeline) {
        Objects.requireNonNull(timeline.getReportDate(), "Timeline report date mustn't be null");
        Map<String, JobParameter> jobParameterMap = new HashMap<>();
        jobParameterMap.put(TIMELINE_ID_PARAM, new JobParameter(timeline.getId()));
        jobParameterMap.put(REPORT_DATE_PARAM, new JobParameter(Date.from(timeline.getReportDate()
                .atStartOfDay()
                .atZone(ZoneId.systemDefault())
                .toInstant())));
        return new JobParameters(jobParameterMap);
    }

    @Override
    protected String getJobName() {
        return DUMMY_TIMELINE_STEP1_JOB;
    }

    @Override
    public List<TimelineStatus> allowedPreviousStepStatusesForStart() {
        return List.of(TimelineStatus.FINISHED, TimelineStatus.FINISHED_WARNING);
    }
}

