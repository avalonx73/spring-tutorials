package com.springtutorials.timeline.common.exception;

public class ProcessTimelineException extends RuntimeException {

    private final Integer code;

    public ProcessTimelineException(String message) {
        super(message);
        this.code = 400; //Default value
    }

    public ProcessTimelineException(String message, Integer code){
        super(message);
        this.code = code;
    }

    public Integer getCode() {
        return code;
    }
}