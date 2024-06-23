package com.springtutorials.timeline.service.process;

import java.util.concurrent.ScheduledFuture;

public interface ProgressUpdatingStepExecution {

    default void calculateAndUpdateStepProgressPercentage() {
        throw new UnsupportedOperationException("Calculating progress step percentage is not supported for current step");
    }

    default Integer getProgressStatus() {
        throw new UnsupportedOperationException("Getting progress status is not supported for current step");
    }

    default void addProgressUpdater(ScheduledFuture<?> progressUpdater) {
        throw new UnsupportedOperationException("Adding progress updater is not supported for current step");
    }

    default long updateProcessStepStateSeconds() {
        return 10;
    }

    default boolean isProgressUpdatingEnable(){
        return false;
    }
}

