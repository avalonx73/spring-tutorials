package com.springtutorials.timeline.common.util;

import com.springtutorials.timeline.common.controller.RequestId;
import com.springtutorials.timeline.common.controller.ResponseWrapper;
import lombok.experimental.UtilityClass;
import org.springframework.http.HttpStatus;

@UtilityClass
public class ApiUtils {

    public static <T> ResponseWrapper<T> wrapPayload(T payload, RequestId requestId) {
        return ResponseWrapper.<T>builder()
                .status(HttpStatus.OK.name())
                .requestId(requestId.getId())
                .data(payload)
                .build();
    }


    public static <T> ResponseWrapper<T> wrapPayload(RequestId requestId) {
        return ResponseWrapper.<T>builder()
                .status(HttpStatus.OK.name())
                .requestId(requestId.getId())
                .build();
    }

    public static <T> ResponseWrapper<T> wrapPayload(T payload, RequestId requestId, HttpStatus status) {
        return ResponseWrapper.<T>builder()
                .status(status.name())
                .requestId(requestId.getId())
                .data(payload)
                .build();
    }

    public static <T> ResponseWrapper<T> wrapPayload(T payload, RequestId requestId, String status, String message) {
        return ResponseWrapper.<T>builder()
                .status(status)
                .message(message)
                .requestId(requestId.getId())
                .data(payload)
                .build();
    }

    public static <T> ResponseWrapper<T> wrapPayload(RequestId requestId, HttpStatus status, String message) {
        return ResponseWrapper.<T>builder()
                .status(status.name())
                .message(message)
                .requestId(requestId.getId())
                .data(null)
                .build();
    }

    public static <T> ResponseWrapper<T> wrapPayload(RequestId requestId, String message) {
        return ResponseWrapper.<T>builder()
                .status(HttpStatus.OK.name())
                .message(message)
                .requestId(requestId.getId())
                .data(null)
                .build();
    }
}

