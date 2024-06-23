package com.springtutorials.timeline.dto.rest.process;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.springtutorials.timeline.common.model.process.ProcessTimelineStepDefinition;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RollbackStepDto {
    private String timelineId;
    private ProcessTimelineStepDefinition stepToRollback;
    @JsonIgnore
    private String requestInitiator;
    private String message;
    private boolean softRollback = false;

}
