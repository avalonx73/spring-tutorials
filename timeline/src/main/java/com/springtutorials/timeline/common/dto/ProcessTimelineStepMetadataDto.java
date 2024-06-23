package com.springtutorials.timeline.common.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.springtutorials.timeline.common.model.process.ProcessTimelineStepDefinition;
import com.springtutorials.timeline.common.model.process.TimelineStatus;
import com.springtutorials.timeline.common.exception.ProcessTimelineException;
import com.springtutorials.timeline.common.model.process.TimelineType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ProcessTimelineStepMetadataDto {

    private String timelineId;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy")
    private LocalDate reportDate;
    private TimelineType type;
    private ProcessTimelineStepDefinition currentStep;
    private ProcessTimelineStepDefinition previousStep;
    private TimelineStatus status;
    private String requestInitiator;
    private String message;
    private boolean forceStart;
    private boolean forceFinish;

    /**
     * Check that this step starts or continues timeline.
     */
    public void isCorrect() {
        if (!((timelineId == null && previousStep == null && TimelineStatus.NEW.equals(status))
                || (timelineId != null && !timelineId.isEmpty() && previousStep != null && !TimelineStatus.NEW.equals(status)))) {
            throw new ProcessTimelineException("Filled fields not suited for the start or continuing timeline");
        }
    }

    /**
     * Check that this step is the first step
     */
    public boolean isFirstStep() {
        return timelineId == null && previousStep == null && TimelineStatus.NEW.equals(status);
    }
}