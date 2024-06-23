package com.springtutorials.timeline.common.exception;

public class ProcessTimelineStepException extends ProcessTimelineException {

    public ProcessTimelineStepException(String message) {
        super(message, 400);
    }

    public ProcessTimelineStepException(String message, Integer code){
        super(message, code);
    }
}
