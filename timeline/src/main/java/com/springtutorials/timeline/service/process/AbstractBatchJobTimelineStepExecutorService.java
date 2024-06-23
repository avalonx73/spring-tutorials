package com.springtutorials.timeline.service.process;

import com.hazelcast.map.IMap;
import com.springtutorials.timeline.common.model.process.ProcessTimeline;
import com.springtutorials.timeline.common.model.process.TimelineStatus;
import com.springtutorials.timeline.common.service.hazelcast.AbstractHazelcastProcessingHelper;
import com.springtutorials.timeline.common.service.hazelcast.HazelcastProcessStepInfo;
import com.springtutorials.timeline.dto.rest.process.FinishStepDto;
import com.springtutorials.timeline.service.BatchJobFacade;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.lang.Nullable;

import java.io.Serializable;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.TimeUnit;

import static com.springtutorials.timeline.common.model.process.TimelineStatus.ERROR;
import static com.springtutorials.timeline.common.model.process.TimelineStatus.FINISHED;
import static com.springtutorials.timeline.common.model.process.TimelineStatus.FINISHED_WARNING;
import static java.lang.String.format;
import static java.util.Objects.nonNull;

@Slf4j
@Getter(AccessLevel.PROTECTED)
public abstract class AbstractBatchJobTimelineStepExecutorService<T extends Serializable> extends AbstractProcessTimelineStepExecutorService<T> {
    private final BatchJobFacade batchJobFacade;

    protected AbstractBatchJobTimelineStepExecutorService(
            AbstractHazelcastProcessingHelper<T> hazelcastHelper,
            ProcessTimelineStepService processTimelineStepService,
            BatchJobFacade batchJobFacade) {
        super(hazelcastHelper, processTimelineStepService);
        this.batchJobFacade = batchJobFacade;
    }

    @Override
    public void startStep(ProcessTimeline timeline) {
        String errMessage = null;
        if (timeline.getReportDate() != null) {
            log.info("Starting {} step of timeline id {} on report date {}", getStepDefinition(), timeline.getId(),
                    timeline.getReportDate());
        } else {
            log.info("Starting {} step of timeline id {} and creation time {}", getStepDefinition(), timeline.getId(),
                    timeline.getCreatedTime().format(DateTimeFormatter.ISO_DATE_TIME));
        }
        HazelcastProcessStepInfo hazelcastStepInfo = prepareHazelcastStepStartInfo(timeline);
        log.info("Starting step {}, hazelcastStepInfo = {}", getStepDefinition(), hazelcastStepInfo);
        try {
            if (hazelcastStepInfo.getJobExecutionId() != null) {
                beforeJobRestart(timeline);
                var jobExecutionId = batchJobFacade.startNextInstance(getJobName());
                hazelcastStepInfo.setJobExecutionId(jobExecutionId.getId());
                errMessage = processJobStatus(jobExecutionId, hazelcastStepInfo);
            } else {
                // TODO Здесь происходит JobInstanceAlreadyCompleteException
                var jobExecution = batchJobFacade.run(getJobName(), createJobParameters(timeline));
                hazelcastStepInfo.setJobExecutionId(jobExecution.getId());
                errMessage = processJobStatus(jobExecution, hazelcastStepInfo);
            }
        } catch (Exception e) {
            log.error("Got next error while executing {} step \n{}", getStepDefinition(), e);
            hazelcastStepInfo.setProcessStepStatus(ERROR);
            errMessage = "Unexpected error occurred: " + e.getMessage();
        } finally {
            FinishStepDto finishStepDto;
            if (errMessage != null) {
                finishStepDto = prepareErrorFinishingProcessMetadata(timeline, errMessage);
            } else if (hazelcastStepInfo.getProcessStepStatus() == FINISHED_WARNING) {
                finishStepDto = prepareSuccessfulFinishingProcessMetadata(timeline, FINISHED_WARNING);
            } else {
                finishStepDto = prepareSuccessfulFinishingProcessMetadata(timeline, FINISHED);
            }
            getHazelcastHelper().getProcessStepInfoMap().put(getStepDefinition().name(), hazelcastStepInfo);
            clearHazelcastDataAndFinishStep(finishStepDto);
        }
    }

    protected void beforeJobRestart(ProcessTimeline timeline) {
        //Normally does nothing. Override if you need some action to be performed before job restart
    }

    protected abstract JobParameters createJobParameters(ProcessTimeline timeline);

    protected abstract String getJobName();

    @Override
    public void calculateAndUpdateStepProgressPercentage() {
        int progressPercentage = getHazelcastHelper().getProgressPercentage();
        getHazelcastHelper().getProgressMap().put(getStepDefinition(), progressPercentage);
        String lockKey = getStepDefinition().name();
        getHazelcastHelper().lockProcessInfoMapAndDoAction(lockKey, () -> {
            IMap<String, HazelcastProcessStepInfo> processStepInfoMap = getHazelcastHelper().getProcessStepInfoMap();
            HazelcastProcessStepInfo processStepInfo = processStepInfoMap.get(lockKey);
            if (nonNull(processStepInfo)) {
                processStepInfo.setProceededRecords(getHazelcastHelper().getProceededRecordCount());
                processStepInfo.setPercentage(progressPercentage);
                processStepInfoMap.set(lockKey, processStepInfo,
                        processStepInfo.getProcessInfoAvailableTime().getSeconds(), TimeUnit.SECONDS);
            }
        });
    }

    @Override
    public Integer getProgressStatus() {
        return getHazelcastHelper().getProgressMap().get(getStepDefinition());
    }

    @Override
    public boolean isProgressUpdatingEnable() {
        return true;
    }

    protected FinishStepDto prepareSuccessfulFinishingProcessMetadata(ProcessTimeline timeline,
                                                                      TimelineStatus status) {
        var finishDto = new FinishStepDto();
        finishDto.setStepToFinish(getStepDefinition());
        finishDto.setTimelineId(timeline.getId());
        finishDto.setStatus(status);
        finishDto.setSoftFinish(true);
        finishDto.setMessage(format("%s successfully finished. Was processed %s records of %s total",
                getStepDefinition(), getHazelcastHelper().getProceededRecordCount(),
                getHazelcastHelper().getAllRecords().size()));
        return finishDto;
    }

    protected FinishStepDto prepareErrorFinishingProcessMetadata(ProcessTimeline timeline,
                                                                 String errMsg) {
        var finishStepDto = new FinishStepDto();
        finishStepDto.setStepToFinish(getStepDefinition());
        finishStepDto.setTimelineId(timeline.getId());
        finishStepDto.setSoftFinish(true);
        finishStepDto.setStatus(ERROR);
        finishStepDto.setMessage(errMsg);
        return finishStepDto;
    }

    @Nullable
    private String processJobStatus(JobExecution jobExecution,
                                    HazelcastProcessStepInfo hazelcastStepInfo) {
        String errMsg = null;
        if (jobExecution.getStatus() == BatchStatus.COMPLETED) {
            hazelcastStepInfo.setProcessStepStatus(FINISHED);
        } else if (jobExecution.getStatus() == BatchStatus.FAILED &&
                jobExecution.getExitStatus().getExitCode().equals("WARNING")) {
            hazelcastStepInfo.setProcessStepStatus(FINISHED_WARNING);
        } else {
            hazelcastStepInfo.setProcessStepStatus(ERROR);
            errMsg = getJobName() + " failed with status = " + jobExecution.getStatus();
        }
        return errMsg;
    }
}

