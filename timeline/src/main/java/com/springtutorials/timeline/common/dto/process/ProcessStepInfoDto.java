package com.springtutorials.timeline.common.dto.process;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.springtutorials.timeline.common.model.process.ProcessEvent;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ProcessStepInfoDto {
    private ProcessEvent event;
    private LocalDateTime eventTime;
    private String eventInitiator;
    private String message;
}