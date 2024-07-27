package com.springtutorials.timeline.dummytimeline;

import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.listener.StepExecutionListenerSupport;

public class DummyTimelineStepExecutionListener extends StepExecutionListenerSupport {
    private final DummyTimelinePartitioner1 partitioner;

    public DummyTimelineStepExecutionListener(DummyTimelinePartitioner1 partitioner) {
        this.partitioner = partitioner;
    }

    @Override
    public void beforeStep(StepExecution stepExecution) {
        partitioner.setStepExecution(stepExecution);
    }
}

