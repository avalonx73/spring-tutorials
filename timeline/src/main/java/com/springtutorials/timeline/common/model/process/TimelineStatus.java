package com.springtutorials.timeline.common.model.process;

public enum TimelineStatus {
    NEW,
    IN_PROCESS,
    WARNING,
    FINISHED_WARNING,
    ERROR,
    STOPPED,
    FINISHED;


    TimelineStatus() {
    }
}
