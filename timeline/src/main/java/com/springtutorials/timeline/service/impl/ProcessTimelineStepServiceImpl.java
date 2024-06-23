package com.springtutorials.timeline.service.impl;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.cp.lock.FencedLock;
import com.springtutorials.timeline.common.dto.ProcessTimelineStepMetadataDto;
import com.springtutorials.timeline.common.exception.ProcessTimelineStepException;
import com.springtutorials.timeline.common.model.process.ProcessEvent;
import com.springtutorials.timeline.common.model.process.ProcessStepInfo;
import com.springtutorials.timeline.common.model.process.ProcessTimeline;
import com.springtutorials.timeline.common.model.process.ProcessTimelineStep;
import com.springtutorials.timeline.common.model.process.ProcessTimelineStepDefinition;
import com.springtutorials.timeline.common.model.process.TimelineStatus;
import com.springtutorials.timeline.common.model.process.TimelineType;
import com.springtutorials.timeline.dto.rest.process.FinishStepDto;
import com.springtutorials.timeline.dto.rest.process.RollbackStepDto;
import com.springtutorials.timeline.factory.ProcessStepTimelineExecutorFactory;
import com.springtutorials.timeline.repository.mongo.ProcessTimelineRepository;
import com.springtutorials.timeline.service.process.AsyncProcessStepExecution;
import com.springtutorials.timeline.service.process.ProcessTimelineService;
import com.springtutorials.timeline.service.process.ProcessTimelineStepExecutorService;
import com.springtutorials.timeline.service.process.ProcessTimelineStepService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static com.springtutorials.timeline.common.model.process.TimelineStatus.FINISHED;
import static com.springtutorials.timeline.common.model.process.TimelineStatus.IN_PROCESS;
import static com.springtutorials.timeline.common.model.process.TimelineStatus.NEW;
import static com.springtutorials.timeline.common.util.StringUtils.nullSafeConcat;
import static java.lang.String.format;

@Slf4j
@Service
public class ProcessTimelineStepServiceImpl implements ProcessTimelineStepService {

    private static final String LOCK_NAME = "PaymentProcessTimelineLock";
    private static final String HAZELCAST_LOCK_FAILURE_MSG_TEMPLATE = "Unable to get lock %s in Hazelcast";
    private static final long LOCK_WAIT_TIMEOUT = 10_000;

    private final ProcessTimelineRepository paymentProcessTimelineRepository;
    private final ProcessTimelineService processTimelineService;
    private final HazelcastInstance hazelcastInstance;
    private final ExecutorService timelineStepExecutorService;
    private final ThreadPoolTaskScheduler updateTimelineProcessInfoExecutor;

    List<ProcessTimelineStepExecutorService> listSteps;
    private Map<ProcessTimelineStepDefinition, ProcessTimelineStepExecutorService> steps;

    public ProcessTimelineStepServiceImpl(ProcessTimelineRepository paymentProcessTimelineRepository,
                                          ProcessTimelineService processTimelineService,
                                          HazelcastInstance hazelcastInstance,
                                          ExecutorService timelineStepExecutorService,
                                          ThreadPoolTaskScheduler updateTimelineProcessInfoExecutor,
                                          @Lazy List<ProcessTimelineStepExecutorService> listSteps
    ) {
        this.paymentProcessTimelineRepository = paymentProcessTimelineRepository;
        this.processTimelineService = processTimelineService;
        this.hazelcastInstance = hazelcastInstance;
        this.timelineStepExecutorService = timelineStepExecutorService;
        this.updateTimelineProcessInfoExecutor = updateTimelineProcessInfoExecutor;
        this.listSteps = listSteps;
    }

    @PostConstruct
    public void afterInit() {
        this.steps = listSteps.stream()
                .collect(Collectors.toMap(ProcessTimelineStepExecutorService::getStepDefinition, value -> value));
    }


    @Override
    @Transactional("mongoTransactionManager")
    public void startStep(ProcessTimelineStepMetadataDto stepMetadataDto) {
        FencedLock lock = this.hazelcastInstance.getCPSubsystem().getLock(LOCK_NAME);
        if (lock.tryLock(LOCK_WAIT_TIMEOUT, TimeUnit.MILLISECONDS)) {
            try {
                ProcessTimeline processTimeline;
                ProcessTimelineStep stepToStart;

                if (stepMetadataDto.isFirstStep()) {
                    processTimeline = getOrCreateCurrentPaymentProcessTimeline(stepMetadataDto.getType(),
                            stepMetadataDto.getReportDate());
                    stepToStart = processTimeline.getPaymentProcessStepByDefinition(stepMetadataDto.getCurrentStep());
                } else {
                    processTimeline = getNotFinishedProcessTimelineById(stepMetadataDto.getTimelineId());
                    stepToStart = processTimeline.getPaymentProcessStepByDefinition(stepMetadataDto.getCurrentStep());

                    throwIfPreviousStepIsNotAllowedStatus(stepMetadataDto, processTimeline, stepToStart);
                }

                if (steps.get(stepToStart.getStepDefinition())
                        .allowedStepStatusesForStart()
                        .contains(stepToStart.getStatus())) {
                    stepToStart.setMessage(null);
                } else {
                    throw new ProcessTimelineStepException(
                            format("Unable to start %s process step. Expected NEW status, got %s",
                                    stepToStart.getStepDefinition(), stepToStart.getStatus()));
                }

                ProcessTimelineStepExecutorService stepExecutorService =
                        ProcessStepTimelineExecutorFactory.getStepExecutorService(stepToStart.getStepDefinition());
                stepToStart.addProcessInfo(ProcessStepInfo.builder()
                        .event(ProcessEvent.STEP_START)
                        .eventTime(LocalDateTime.now())
                        .eventInitiator(stepMetadataDto.getRequestInitiator())
                        .build());
                stepToStart.setStatus(IN_PROCESS);
                processTimeline.setTimelineStatus(IN_PROCESS);
                paymentProcessTimelineRepository.save(processTimeline);

                executeStep(processTimeline, stepToStart, stepExecutorService);
            } finally {
                lock.unlock();
            }
        } else {
            throw new ProcessTimelineStepException(format(HAZELCAST_LOCK_FAILURE_MSG_TEMPLATE, LOCK_NAME), 409);
        }
    }

    private void throwIfPreviousStepIsNotAllowedStatus(ProcessTimelineStepMetadataDto stepMetadataDto,
                                                       ProcessTimeline processTimeline,
                                                       ProcessTimelineStep stepToStart) {
        if (!stepMetadataDto.isForceStart()) {
            ProcessTimelineStep previousStep =
                    processTimeline.getPaymentProcessStepByDefinition(stepMetadataDto.getPreviousStep());
            if (!steps.get(stepToStart.getStepDefinition())
                    .allowedPreviousStepStatusesForStart()
                    .contains(previousStep.getStatus())) {
                throw new ProcessTimelineStepException(
                        format("Unable to start step %s till step %s is no done yet",
                                stepToStart.getStepDefinition(), previousStep.getStepDefinition()));
            }
        }
    }

    private void executeStep(ProcessTimeline processTimeline, ProcessTimelineStep stepToStart,
                             ProcessTimelineStepExecutorService stepExecutorService) {
        timelineStepExecutorService.submit(() -> {
            log.info("Starting executor service for step {}", stepToStart.getStepDefinition());
            try {
                if (stepExecutorService.isProgressUpdatingEnable()) {
                    long updateProcessStepStateSeconds = stepExecutorService.updateProcessStepStateSeconds();
                    ScheduledFuture<?> progressUpdater =
                            updateTimelineProcessInfoExecutor.scheduleWithFixedDelay(
                                    stepExecutorService::calculateAndUpdateStepProgressPercentage,
                                    Instant.now().plusSeconds(updateProcessStepStateSeconds),
                                    Duration.ofSeconds(updateProcessStepStateSeconds));
                    if (stepExecutorService instanceof AsyncProcessStepExecution) {
                        log.info("Starting progress updater in async execution");
                        stepExecutorService.addProgressUpdater(progressUpdater);
                        stepExecutorService.startStep(processTimeline);
                    } else {
                        try {
                            stepExecutorService.startStep(processTimeline);
                        } finally {
                            if (!progressUpdater.isDone() || !progressUpdater.isCancelled()) {
                                progressUpdater.cancel(false);
                            }
                        }
                    }
                } else {
                    log.info("Starting {} step without progress updating", stepToStart.getStepDefinition());
                    stepExecutorService.startStep(processTimeline);
                }
            } catch (Exception e) {
                log.error("Got error while executing step \n {}", e.getMessage());
            }
        });
    }

    @Override
    @Transactional("mongoTransactionManager")
    public void finishStep(FinishStepDto finishStepDto) {
        FencedLock lock = this.hazelcastInstance.getCPSubsystem().getLock(LOCK_NAME);
        if (lock.tryLock(LOCK_WAIT_TIMEOUT, TimeUnit.MILLISECONDS)) {
            try {
                ProcessTimeline processTimeline = getNotFinishedProcessTimelineById(finishStepDto.getTimelineId());
                ProcessTimelineStep processTimelineStep =
                        processTimeline.getPaymentProcessStepByDefinition(finishStepDto.getStepToFinish());
                ProcessTimelineStepExecutorService stepExecutorService =
                        ProcessStepTimelineExecutorFactory.getStepExecutorService(finishStepDto.getStepToFinish());
                if (stepExecutorService.isSpecialStepOperationsSupported() && !finishStepDto.isSoftFinish()) {
                    log.info("Handle force step finishing for step {} with  params {}", processTimelineStep,
                            finishStepDto);
                    stepExecutorService.stopStep(processTimeline);
                } else {
                    processTimelineStep.setStatus(finishStepDto.getStatus());
                }
                processTimelineStep.addProcessInfo(ProcessStepInfo.builder()
                        .event(ProcessEvent.STEP_END)
                        .eventTime(LocalDateTime.now())
                        .eventInitiator(finishStepDto.getRequestInitiator())
                        .message(nullSafeConcat(processTimelineStep.getMessage(),
                                finishStepDto.getMessage()))
                        .build());
                processTimelineStep.setMessage(
                        nullSafeConcat(processTimelineStep.getMessage(), finishStepDto.getMessage()));
                paymentProcessTimelineRepository.save(processTimeline);
            } finally {
                lock.unlock();
            }

        } else {
            throw new ProcessTimelineStepException(HAZELCAST_LOCK_FAILURE_MSG_TEMPLATE, 409);
        }
    }

    @Override
    @Transactional("mongoTransactionManager")
    public void updateStepMessage(ProcessTimelineStepMetadataDto stepMetadataDto) {
        FencedLock lock = this.hazelcastInstance.getCPSubsystem().getLock(LOCK_NAME);
        if (lock.tryLock(LOCK_WAIT_TIMEOUT, TimeUnit.MILLISECONDS)) {
            try {
                var processTimeline = getNotFinishedProcessTimelineById(stepMetadataDto.getTimelineId());
                var processTimelineStep =
                        processTimeline.getPaymentProcessStepByDefinition(stepMetadataDto.getCurrentStep());
                processTimelineStep.setMessage(
                        nullSafeConcat(processTimelineStep.getMessage(), stepMetadataDto.getMessage()));
                paymentProcessTimelineRepository.save(processTimeline);
            } finally {
                lock.unlock();
            }

        } else {
            throw new ProcessTimelineStepException(HAZELCAST_LOCK_FAILURE_MSG_TEMPLATE, 409);
        }
    }

    @Override
    @Transactional("mongoTransactionManager")
    public void updateStepStatus(ProcessTimelineStepMetadataDto stepMetadataDto) {
        FencedLock lock = this.hazelcastInstance.getCPSubsystem().getLock(LOCK_NAME);
        if (lock.tryLock(LOCK_WAIT_TIMEOUT, TimeUnit.MILLISECONDS)) {
            try {
                ProcessTimeline processTimeline = getNotFinishedProcessTimelineById(stepMetadataDto.getTimelineId());
                ProcessTimelineStep processTimelineStep =
                        processTimeline.getPaymentProcessStepByDefinition(stepMetadataDto.getCurrentStep());
                processTimelineStep.setStatus(stepMetadataDto.getStatus());
                processTimelineStep.setMessage(
                        nullSafeConcat(processTimelineStep.getMessage(), stepMetadataDto.getMessage()));
                paymentProcessTimelineRepository.save(processTimeline);
            } finally {
                lock.unlock();
            }

        } else {
            throw new ProcessTimelineStepException(HAZELCAST_LOCK_FAILURE_MSG_TEMPLATE, 409);
        }
    }

    @Override
    @Transactional("mongoTransactionManager")
    public void rollBackStep(RollbackStepDto rollbackStepDto) {
        FencedLock lock = this.hazelcastInstance.getCPSubsystem().getLock(LOCK_NAME);
        if (lock.tryLock(LOCK_WAIT_TIMEOUT, TimeUnit.MILLISECONDS)) {
            try {
                ProcessTimeline processTimeline = getNotFinishedProcessTimelineById(rollbackStepDto.getTimelineId());
                ProcessTimelineStep processTimelineStep =
                        processTimeline.getPaymentProcessStepByDefinition(rollbackStepDto.getStepToRollback());
                processTimelineStep.setStatus(NEW);
                if (processTimelineStep.isFirstStep()) {
                    processTimeline.setTimelineStatus(NEW);
                }
                ProcessTimelineStepExecutorService stepExecutorService =
                        ProcessStepTimelineExecutorFactory.getStepExecutorService(rollbackStepDto.getStepToRollback());
                if (stepExecutorService.isSpecialStepOperationsSupported() && !rollbackStepDto.isSoftRollback()) {
                    log.info("Handle force step rollback with for step {} with params {}", processTimelineStep,
                            rollbackStepDto);
                    stepExecutorService.rollBackStep(processTimeline);
                } else {
                    log.info("Step {} doesn't support rollback so just resetting its status to NEW",
                            processTimelineStep.getStepDefinition());
                }
                processTimelineStep.addProcessInfo(ProcessStepInfo.builder()
                        .event(ProcessEvent.STEP_ROLLBACK)
                        .eventTime(LocalDateTime.now())
                        .eventInitiator(rollbackStepDto.getRequestInitiator())
                        .message(nullSafeConcat(processTimelineStep.getMessage(),
                                rollbackStepDto.getMessage()))
                        .build());
                processTimelineStep.setMessage(null);
                paymentProcessTimelineRepository.save(processTimeline);
            } finally {
                lock.unlock();
            }

        } else {
            throw new ProcessTimelineStepException(HAZELCAST_LOCK_FAILURE_MSG_TEMPLATE, 409);
        }
    }

    private ProcessTimeline getNotFinishedProcessTimelineById(String id) {
        var processTimeline = getProcessTimelineById(id);
        throwIfTimelineIsFinished(processTimeline);
        return processTimeline;
    }

    private ProcessTimeline getOrCreateCurrentPaymentProcessTimeline(TimelineType type,
                                                                     LocalDate reportDate) {
        var processTimeline = processTimelineService.getOrCreateCurrentTimeline(type, reportDate);
        throwIfTimelineIsFinished(processTimeline);
        return processTimeline;
    }

    private ProcessTimeline getProcessTimelineById(String id) {
        Optional<ProcessTimeline> paymentProcessTimelineOptional = paymentProcessTimelineRepository.findById(id);
        return paymentProcessTimelineOptional.orElseThrow(
                () -> new ProcessTimelineStepException(format("PaymentProcessTimeline was not found by %s id", id)));
    }


    private void throwIfTimelineIsFinished(ProcessTimeline processTimeline) {
        if (FINISHED.equals(processTimeline.getTimelineStatus()) || TimelineStatus.FINISHED_WARNING.equals(processTimeline.getTimelineStatus())) {
            throw new ProcessTimelineStepException(
                    "Unable to modify PaymentProcessTimeline. Timeline already finished.");
        }
    }
}
