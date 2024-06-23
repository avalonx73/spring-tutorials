package com.springtutorials.timeline.service.process;

import com.springtutorials.timeline.common.model.process.ProcessTimeline;
import com.springtutorials.timeline.common.model.process.ProcessTimelineStepDefinition;
import com.springtutorials.timeline.common.model.process.TimelineStatus;

import java.util.List;

public interface ProcessTimelineStepExecutorService extends ProgressUpdatingStepExecution {

    void startStep(ProcessTimeline timeline);

    ProcessTimelineStepDefinition getStepDefinition();

    default List<TimelineStatus> allowedStepStatusesForStart() {
        return List.of(TimelineStatus.NEW);
    }

    default List<TimelineStatus> allowedPreviousStepStatusesForStart() {
        return List.of(TimelineStatus.FINISHED);
    }

    default void stopStep(ProcessTimeline timeline) {
        if (!isSpecialStepOperationsSupported()) {
            throw new UnsupportedOperationException("Special operations is not supported");
        }
    }

    default void rollBackStep(ProcessTimeline timeline) {
        if (!isSpecialStepOperationsSupported()) {
            throw new UnsupportedOperationException("Special operations is not supported");
        }
    }

    default boolean isSpecialStepOperationsSupported() {
        return false;
    }
}
