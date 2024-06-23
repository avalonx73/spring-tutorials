package com.springtutorials.timeline.common.exception;

public class DocumentNotFoundException extends RuntimeException {
    private static final long serialVersionUID = 8625564526811341863L;

    public DocumentNotFoundException(String message) {
        super(message);
    }
}


