package com.springtutorials.timeline.service.impl;

import com.springtutorials.timeline.common.exception.DocumentNotFoundException;
import com.springtutorials.timeline.service.BatchJobFacade;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersInvalidException;
import org.springframework.batch.core.configuration.JobRegistry;
import org.springframework.batch.core.explore.JobExplorer;
import org.springframework.batch.core.launch.JobExecutionNotRunningException;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.launch.JobOperator;
import org.springframework.batch.core.launch.JobParametersNotFoundException;
import org.springframework.batch.core.launch.NoSuchJobException;
import org.springframework.batch.core.launch.NoSuchJobExecutionException;
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.repository.JobRestartException;
import org.springframework.stereotype.Service;

import static java.lang.String.format;

@Service
@RequiredArgsConstructor
@Slf4j
public class BatchJobFacadeImpl implements BatchJobFacade {
    private final JobLauncher jobLauncher;
    private final JobExplorer jobExplorer;
    private final JobOperator jobOperator;
    private final JobRegistry jobRegistry;

    @Override
    public JobExecution run(String jobName, JobParameters jobParameters)
            throws NoSuchJobException, JobInstanceAlreadyCompleteException, JobExecutionAlreadyRunningException,
            JobParametersInvalidException, JobRestartException {
        var job = jobRegistry.getJob(jobName);
        return jobLauncher.run(job, jobParameters);
    }

    @Override
    public void stop(long jobExecutionId) {
        var jobExecution = jobExplorer.getJobExecution(jobExecutionId);
        if (jobExecution != null && jobExecution.isRunning()) {
            try {
                jobOperator.stop(jobExecution.getId());
            } catch (NoSuchJobExecutionException | JobExecutionNotRunningException e) {
                log.warn("Unexpected error while stopping the job", e);
            }
        }
    }

    @Override
    public JobExecution startNextInstance(String jobName)
            throws JobInstanceAlreadyCompleteException, NoSuchJobException, JobExecutionAlreadyRunningException,
            JobParametersInvalidException, JobParametersNotFoundException, JobRestartException {
        Long jobExecutionId = jobOperator.startNextInstance(jobName);
        return getJobExecutionById(jobExecutionId);
    }

    private JobExecution getJobExecutionById(long jobExecutionId) {
        var jobExecution = jobExplorer.getJobExecution(jobExecutionId);
        if (jobExecution == null) {
            throw new DocumentNotFoundException(format("Failed to get JobExecution for id = '%d'",
                    jobExecutionId));
        }
        return jobExecution;
    }
}

