package com.springtutorials.timeline.common.service.hazelcast;

import com.springtutorials.timeline.common.dto.ProcessTimelineStepMetadataDto;
import com.springtutorials.timeline.common.model.process.TimelineStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.lang.Nullable;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.time.Duration;

import static java.time.format.DateTimeFormatter.ISO_DATE;
import static java.time.temporal.ChronoUnit.HOURS;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class HazelcastProcessStepInfo implements Externalizable {
    private static final long serialVersionUID = -8264007909712654538L;

    private String timelineId;
    private String processName;
    private String processStepName;
    private Integer percentage;
    private TimelineStatus processStepStatus;
    private String reportDate;
    private String startedAt;
    private String endedAt;
    private Integer recordsForProceed;
    private Integer proceededRecords;
    private String message;
    @Nullable
    private Long jobExecutionId;
    private Duration processInfoAvailableTime = Duration.of(2, HOURS);

    public static HazelcastProcessStepInfo createFromStep(ProcessTimelineStepMetadataDto metadataDto) {
        var processStepInfo = new HazelcastProcessStepInfo();
        processStepInfo.setProcessName(metadataDto.getType().name());
        processStepInfo.setProcessStepName(metadataDto.getCurrentStep().name());
        processStepInfo.setReportDate(ISO_DATE.format(metadataDto.getReportDate()));
        processStepInfo.setPercentage(0);
        return processStepInfo;
    }

    @Override
    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeObject(this.timelineId);
        out.writeObject(this.processName);
        out.writeObject(this.processStepName);
        out.writeObject(this.percentage);
        out.writeObject(this.processStepStatus);
        out.writeObject(this.reportDate);
        out.writeObject(this.startedAt);
        out.writeObject(this.endedAt);
        out.writeObject(this.recordsForProceed);
        out.writeObject(this.proceededRecords);
        out.writeObject(this.message);
        out.writeObject(this.jobExecutionId);
        out.writeObject(this.processInfoAvailableTime.getSeconds());
    }

    @Override
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        this.timelineId = (String) in.readObject();
        this.processName = (String) in.readObject();
        this.processStepName = (String) in.readObject();
        this.percentage = (Integer) in.readObject();
        this.processStepStatus = (TimelineStatus) in.readObject();
        this.reportDate = (String) in.readObject();
        this.startedAt = (String) in.readObject();
        this.endedAt = (String) in.readObject();
        this.recordsForProceed = (Integer) in.readObject();
        this.proceededRecords = (Integer) in.readObject();
        this.message = (String) in.readObject();
        this.jobExecutionId = (Long) in.readObject();
        this.processInfoAvailableTime = Duration.ofSeconds((Long) in.readObject());
    }
}
