package com.springtutorials.timeline.common.model.process;

import com.springtutorials.timeline.common.exception.ProcessTimelineStepException;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Version;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.lang.Nullable;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import static java.lang.String.format;

@Data
@NoArgsConstructor
@Document(collection = ProcessTimeline.PAYMENT_PROCESS_TIMELINE)
public class ProcessTimeline {

    public static final String PAYMENT_PROCESS_TIMELINE = "payment_process_timeline";

    @Id
    private String id;

    private TimelineType type;

    /**
     * Reporting date of process steps
     * in dd-MM-yyyy format
     */
    @Nullable
    private LocalDate reportDate;

    /**
     * Timeline process status
     */
    private TimelineStatus timelineStatus;

    /**
     * Date and time of closing the timeline
     */
    private LocalDateTime finishTime;

    @CreatedDate
    private LocalDateTime createdTime;

    private List<ProcessTimelineStep> processSteps;

    @Version
    private Long version;


    public static ProcessTimeline createEmptyPaymentProcessTimelineForPeriod(TimelineType timelineType, LocalDate reportDate) {
        var processTimeline = new ProcessTimeline();
        List<ProcessTimelineStep> processSteps = Arrays.stream(ProcessTimelineStepDefinition.values())
                .filter(e -> e.getType().equals(timelineType))
                .sorted(Comparator.comparingInt(ProcessTimelineStepDefinition::getOrder))
                .map(e -> new ProcessTimelineStep(e, e.getOrder(), e.getCode(), new ArrayList<>(), TimelineStatus.NEW, null))
                .collect(Collectors.toList());
        processTimeline.setType(timelineType);
        processTimeline.setReportDate(reportDate);
        processTimeline.setProcessSteps(processSteps);
        processTimeline.setTimelineStatus(TimelineStatus.NEW);
        return processTimeline;
    }

    public static ProcessTimeline createEmptyPaymentProcessTimelineForPeriodAndId(TimelineType timelineType,
                                                                                  LocalDate reportDate, String id) {
        var processTimeline = createEmptyPaymentProcessTimelineForPeriod(timelineType, reportDate);
        processTimeline.setId(id);
        return processTimeline;
    }

    public ProcessTimelineStep getPaymentProcessStepByDefinition(ProcessTimelineStepDefinition definition) {
        return processSteps.stream()
                .filter(e -> e.getStepDefinition().equals(definition))
                .findAny()
                .orElseThrow(() -> new ProcessTimelineStepException(format("ProcessStep not found by definition: %s", definition.name())));
    }

    public boolean isFinished() {
        return timelineStatus == TimelineStatus.FINISHED || timelineStatus == TimelineStatus.FINISHED_WARNING;
    }
}
