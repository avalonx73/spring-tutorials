package com.springtutorials.timeline.dto.rest.process;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.springtutorials.timeline.common.model.process.ProcessTimelineStepDefinition;
import com.springtutorials.timeline.common.model.process.TimelineStatus;
import lombok.Data;

@Data
public class FinishStepDto {
    private String timelineId;
    private ProcessTimelineStepDefinition stepToFinish;
    @JsonIgnore
    private String requestInitiator;
    private TimelineStatus status = TimelineStatus.FINISHED;
    private String message;
    private boolean softFinish = false;
}