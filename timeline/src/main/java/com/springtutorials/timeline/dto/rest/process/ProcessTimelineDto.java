package com.springtutorials.timeline.dto.rest.process;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.springtutorials.timeline.common.model.process.TimelineStatus;
import com.springtutorials.timeline.common.model.process.TimelineType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ProcessTimelineDto {
    private String id;
    private TimelineType type;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy")
    private LocalDate reportDate;
    private LocalDateTime createdTime;
    private LocalDateTime finishTime;
    private TimelineStatus timelineStatus;
    private List<ProcessStepDto> processSteps;
}

