package com.springtutorials.timeline.common.model.process;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProcessTimelineStep {
    /**
     * Determining the step of the business process
     */
    private ProcessTimelineStepDefinition stepDefinition;

    /**
     * The order of a step in a business process
     */
    private int order;

    private String stepName;

    /**
     * Timeline step process info such as START/END/ROLLBACK step
     */
    private List<ProcessStepInfo> processInfo;

    /**
     * Timeline step execution status
     */
    private TimelineStatus status;

    /**
     * Additional information on performing a timeline step
     */
    private String message;

    public void addProcessInfo(ProcessStepInfo processInfo) {
        this.processInfo.add(processInfo);
    }

    public boolean isFirstStep() {
        return order == 0;
    }

    public boolean isFinished() {
        return status == TimelineStatus.FINISHED || status == TimelineStatus.FINISHED_WARNING;
    }

    public boolean isNew() {
        return status == TimelineStatus.NEW;
    }
}

