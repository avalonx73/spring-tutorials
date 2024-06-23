package com.springtutorials.timeline.batch.process;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.StepExecutionListener;
import org.springframework.batch.core.step.tasklet.Tasklet;

@Slf4j
@Getter
@Setter
public abstract class StateTasklet implements Tasklet, StepExecutionListener {
    private TaskState state = TaskState.SUCCESS;

    @Override
    public void beforeStep(StepExecution stepExecution) {
        // We don't need any setup actions
    }

    @Override
    public ExitStatus afterStep(StepExecution stepExecution) {
        switch (state) {
            case SUCCESS: {
                return null;
            }
            case ERROR: {
                stepExecution.setStatus(BatchStatus.FAILED);
                return ExitStatus.FAILED;
            }
            case WARNING: {
                stepExecution.setStatus(BatchStatus.FAILED);
                return new ExitStatus("WARNING");
            }
            default: {
                log.warn("Unknown tasklet state {}", state);
                return null;
            }
        }
    }
}

