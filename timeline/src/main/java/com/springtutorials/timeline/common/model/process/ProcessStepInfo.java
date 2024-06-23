package com.springtutorials.timeline.common.model.process;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Data
@AllArgsConstructor
@Builder
@NoArgsConstructor
public class ProcessStepInfo {
    private ProcessEvent event;
    private LocalDateTime eventTime;
    private String eventInitiator;
    private String message;
    private Map<String, Object> additionalData;

    public ProcessStepInfo(ProcessEvent event, LocalDateTime eventTime, String eventInitiator, String message) {
        this(event, eventTime, eventInitiator, message, new HashMap<>());
    }
}
