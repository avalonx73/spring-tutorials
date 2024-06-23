package com.springtutorials.timeline.service;

import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersInvalidException;
import org.springframework.batch.core.launch.JobParametersNotFoundException;
import org.springframework.batch.core.launch.NoSuchJobException;
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.repository.JobRestartException;

public interface BatchJobFacade {

    JobExecution run(String jobName, JobParameters jobParameters)
            throws NoSuchJobException, JobInstanceAlreadyCompleteException, JobExecutionAlreadyRunningException,
            JobParametersInvalidException, JobRestartException;

    void stop(long jobExecutionId);

    JobExecution startNextInstance(String jobName)
            throws JobInstanceAlreadyCompleteException, NoSuchJobException, JobExecutionAlreadyRunningException,
            JobParametersInvalidException, JobParametersNotFoundException, JobRestartException;
}
