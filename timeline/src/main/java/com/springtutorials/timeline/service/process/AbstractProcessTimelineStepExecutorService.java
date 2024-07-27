package com.springtutorials.timeline.service.process;

import com.hazelcast.map.IMap;
import com.hazelcast.query.Predicate;
import com.springtutorials.timeline.common.model.process.ProcessTimeline;
import com.springtutorials.timeline.common.service.hazelcast.AbstractHazelcastProcessingHelper;
import com.springtutorials.timeline.common.service.hazelcast.HazelcastProcessStepInfo;
import com.springtutorials.timeline.common.service.hazelcast.ProcessStepEntry;
import com.springtutorials.timeline.dto.rest.process.FinishStepDto;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

import static com.springtutorials.timeline.common.model.process.TimelineStatus.IN_PROCESS;
import static java.time.format.DateTimeFormatter.ISO_DATE;
import static java.time.format.DateTimeFormatter.ISO_DATE_TIME;
import static java.util.Objects.nonNull;

@Slf4j
@RequiredArgsConstructor
@Getter(AccessLevel.PROTECTED)
public abstract class AbstractProcessTimelineStepExecutorService<T extends Serializable>
        implements ProcessTimelineStepExecutorService {
    private final AbstractHazelcastProcessingHelper<T> hazelcastHelper;
    private final ProcessTimelineStepService processTimelineStepService;

    public void clearHazelcastDataAndFinishStep(FinishStepDto finishStepDto) {
        updateHzInfo(finishStepDto);
        log.info("Finishing {} step with status {}", getStepDefinition(), finishStepDto.getStatus());
        processTimelineStepService.finishStep(finishStepDto);
        clearHazelcastDataOnFinish();
    }

    public void updateHzInfo(FinishStepDto finishStepDto) {
        String lockKey = getStepDefinition().name();
        getHazelcastHelper().lockProcessInfoMapAndDoAction(lockKey, () -> {
            IMap<String, HazelcastProcessStepInfo> processStepInfoMap = getHazelcastHelper().getProcessStepInfoMap();
            HazelcastProcessStepInfo processStepInfo = processStepInfoMap.get(lockKey);
            if (nonNull(processStepInfo)) {
                processStepInfo.setEndedAt(ISO_DATE_TIME.format(LocalDateTime.now()));
                processStepInfo.setProcessStepStatus(finishStepDto.getStatus());
                processStepInfo.setPercentage(100);
                processStepInfo.setProceededRecords(getHazelcastHelper().getProceededRecordCount());
                processStepInfo.setMessage(finishStepDto.getMessage());
                processStepInfoMap.set(lockKey, processStepInfo, processStepInfo.getProcessInfoAvailableTime().getSeconds(),
                        TimeUnit.SECONDS);
            }
        });
    }

    protected Predicate<String, ProcessStepEntry<T>> getRecordsToProceedPredicate() {
        return mapEntry -> true;
    }

    protected HazelcastProcessStepInfo prepareHazelcastStepStartInfo(ProcessTimeline timeline) {
        HazelcastProcessStepInfo hazelcastStepInfo = getOrCreateStepInfo(timeline);
        hazelcastStepInfo.setTimelineId(timeline.getId());
        hazelcastStepInfo.setProcessName(getStepDefinition().getType().name());
        hazelcastStepInfo.setProcessStepName(getStepDefinition().name());
        hazelcastStepInfo.setPercentage(0);

        if (timeline.getReportDate() != null) {
            hazelcastStepInfo.setReportDate(timeline.getReportDate().format(ISO_DATE));
        }

        hazelcastStepInfo.setStartedAt(ISO_DATE_TIME.format(LocalDateTime.now()));
        hazelcastStepInfo.setProcessStepStatus(IN_PROCESS);
        getHazelcastHelper().getProcessStepInfoMap().put(getStepDefinition().name(), hazelcastStepInfo);
        return hazelcastStepInfo;
    }

    //TODO
    private HazelcastProcessStepInfo getOrCreateStepInfo(ProcessTimeline timeline) {
        HazelcastProcessStepInfo hazelcastStepInfo = getHazelcastHelper()
                .getProcessStepInfoMap().get(getStepDefinition().name());

        if (hazelcastStepInfo != null && timeline.getId().equals(hazelcastStepInfo.getTimelineId())) {
            return hazelcastStepInfo;
        }

        return new HazelcastProcessStepInfo();
    }

    protected void clearHazelcastDataOnFinish() {
        log.info("Clearing hazelcast {} timeline step information data", getStepDefinition());
        hazelcastHelper.clearRecordsMap();
        hazelcastHelper.getProgressMap().remove(getStepDefinition());
    }
}
