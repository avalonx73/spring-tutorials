package com.springtutorials.timeline.dto.rest.process;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.springtutorials.timeline.common.dto.process.ProcessStepInfoDto;
import com.springtutorials.timeline.common.model.process.TimelineStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ProcessStepDto {
    private String stepDefinitionCode;
    private Integer order;
    private String description;
    private List<ProcessStepInfoDto> processStepInfo;
    private TimelineStatus status;
    private String message;
}