package com.springtutorials.timeline.service.impl;

import com.springtutorials.timeline.common.exception.DocumentNotFoundException;
import com.springtutorials.timeline.common.exception.ProcessTimelineException;
import com.springtutorials.timeline.common.exception.ProcessTimelineStepException;
import com.springtutorials.timeline.common.model.process.ProcessStepInfo;
import com.springtutorials.timeline.common.model.process.ProcessTimeline;
import com.springtutorials.timeline.common.model.process.ProcessTimelineStep;
import com.springtutorials.timeline.common.model.process.TimelineType;
import com.springtutorials.timeline.dto.rest.process.ProcessTimelineDto;
import com.springtutorials.timeline.mapper.ProcessTimelineMapper;
import com.springtutorials.timeline.repository.mongo.ProcessTimelineRepository;
import com.springtutorials.timeline.service.process.ProcessTimelineService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.springtutorials.timeline.common.model.process.ProcessTimeline.createEmptyPaymentProcessTimelineForPeriod;
import static com.springtutorials.timeline.common.model.process.TimelineStatus.FINISHED;
import static com.springtutorials.timeline.common.model.process.TimelineStatus.FINISHED_WARNING;
import static com.springtutorials.timeline.common.model.process.TimelineStatus.NEW;
import static com.springtutorials.timeline.common.model.process.TimelineType.DUMMY_TIMELINE;
import static java.lang.String.format;
import static org.springframework.data.mongodb.core.query.Criteria.where;

@Slf4j
@Service
public class ProcessTimelineServiceImpl implements ProcessTimelineService {

    private final ProcessTimelineMapper processTimelineMapper;
    private final ProcessTimelineRepository processTimelineRepository;

    private final MongoTemplate mongoTemplate;

    private static final String PROCESS_NOT_FOUND_BY_TYPE_AND_ID = "Process timeline with id %s not found";
    private static final String PROCESS_NOT_FOUND_BY_REPORT_DATE = "Process timeline of %s not found";

    public ProcessTimelineServiceImpl(ProcessTimelineMapper processTimelineMapper,
                                      ProcessTimelineRepository processTimelineRepository,
                                      MongoTemplate mongoTemplate) {
        this.processTimelineMapper = processTimelineMapper;
        this.processTimelineRepository = processTimelineRepository;
        this.mongoTemplate = mongoTemplate;
    }

    @Override
    public ProcessTimelineDto findOrCreateCurrentTimeline(TimelineType type, LocalDate reportDate) {
        return processTimelineMapper.toDto(getOrCreateNewTimelineInternal(type, reportDate));
    }

    @Override
    public ProcessTimeline getOrCreateCurrentTimeline(TimelineType type, LocalDate reportDate) {
        return getOrCreateNewTimelineInternal(type, reportDate);
    }

    @Override
    public ProcessTimelineDto findByReportDate(LocalDate reportDate) {
        return processTimelineMapper.toDto(processTimelineRepository.findByReportDate(reportDate)
                .orElseThrow(() -> new ProcessTimelineException(format(PROCESS_NOT_FOUND_BY_REPORT_DATE, reportDate))));
    }

    @Override
    public ProcessTimelineDto findByTypeAndReportDate(TimelineType type, LocalDate reportDate) {
        ProcessTimeline processTimeline = processTimelineRepository.findByTypeAndReportDate(type.name(), reportDate)
                .orElseThrow(() -> new ProcessTimelineException(format(PROCESS_NOT_FOUND_BY_REPORT_DATE, reportDate)));
        processTimeline.getProcessSteps()
                .forEach(processTimelineStep -> processTimelineStep.getProcessInfo()
                        .sort(Comparator.comparing(ProcessStepInfo::getEventTime).reversed()));
        return processTimelineMapper.toDto(processTimeline);
    }

    @Override
    public Page<ProcessTimelineDto> findAllByReportMonthAndType(YearMonth reportMonth, TimelineType type, Pageable pageable) {
        LocalDate startOfMonth = reportMonth.atDay(1);
        LocalDate endOfMonth = reportMonth.atEndOfMonth();
        Query query = new Query();
        query.addCriteria(where("type").is(type).and("reportDate").gte(startOfMonth).lte(endOfMonth));

        long totalCount = mongoTemplate.count(query, ProcessTimeline.class);

        query.with(pageable).with(Sort.by(Sort.Direction.DESC, "reportDate"));
        List<ProcessTimeline> allByReportMonth = mongoTemplate.find(query, ProcessTimeline.class);

        return new PageImpl<>(
                allByReportMonth.stream()
                        .map(processTimelineMapper::toDto)
                        .collect(Collectors.toList()),
                pageable,
                totalCount
        );
    }

    @Override
    public Page<ProcessTimelineDto> findAllByType(TimelineType type, Pageable pageable) {
        Page<ProcessTimeline> allProcessByType = processTimelineRepository.findAllByType(type, pageable);
        List<ProcessTimelineDto> processTimelineDtos = allProcessByType.getContent().stream()
                .map(processTimelineMapper::toDto)
                .collect(Collectors.toList());
        return new PageImpl<>(
                processTimelineDtos,
                pageable,
                processTimelineDtos.size()
        );
    }

    @Override
    public ProcessTimelineDto findById(String id) {
        return processTimelineMapper.toDto(processTimelineRepository.findById(id)
                .orElseThrow(() -> new ProcessTimelineException(format(PROCESS_NOT_FOUND_BY_TYPE_AND_ID, id))));
    }

    @Override
    @Transactional("mongoTransactionManager")
    public void delete(String timelineId) {
        var processTimeline = processTimelineRepository.findById(timelineId)
                .orElseThrow(() -> new DocumentNotFoundException(format("Process timeline with id %s doesn't exist", timelineId)));

        var timelineStatus = processTimeline.getTimelineStatus();

        if (timelineStatus != NEW && timelineStatus != FINISHED && timelineStatus != FINISHED_WARNING) {
            throw new ProcessTimelineException(format("You can't delete the process timeline with id %s because its status is not NEW or FINISHED or FINISHED_WARNING", timelineId));
        }

        if (!processTimeline.getProcessSteps().isEmpty() &&
                !processTimeline.getProcessSteps().stream()
                        .allMatch(step -> {
                            if (timelineStatus == NEW) {
                                return (step.getStatus() == NEW);
                            } else if (timelineStatus == FINISHED || timelineStatus == FINISHED_WARNING) {
                                return (step.getStatus() == FINISHED || step.getStatus() == FINISHED_WARNING);
                            } else {
                                return false;
                            }
                        })) {
            throw new ProcessTimelineException(format("You can't delete the process timeline with id %s because it contains steps with a status is not NEW or FINISHED or FINISHED_WARNING", timelineId));
        }

        processTimelineRepository.deleteById(timelineId);
    }

    @Override
    @Transactional("mongoTransactionManager")
    public void finishTimeline(String timelineId) {
        ProcessTimeline processTimeline = processTimelineRepository.findById(timelineId)
                .orElseThrow(() -> new DocumentNotFoundException(format("Process timeline with id %s doesn't exist", timelineId)));

        var timelineStatus = processTimeline.getTimelineStatus();

        if (NEW.equals(timelineStatus) || FINISHED.equals(timelineStatus)) {
            throw new ProcessTimelineException(format("You can't finish the process timeline with id %s because its status is %s", timelineId, timelineStatus));
        }

        List<ProcessTimelineStep> processSteps = processTimeline.getProcessSteps();
        if (!processSteps.isEmpty() && processSteps.stream()
                .allMatch(processTimelineStep -> FINISHED.equals(processTimelineStep.getStatus())
                        || FINISHED_WARNING.equals(processTimelineStep.getStatus()))) {
            processTimeline.setTimelineStatus(FINISHED);
            processTimeline.setFinishTime(LocalDateTime.now());
            processTimelineRepository.save(processTimeline);
        } else {
            throw new ProcessTimelineException(format("You can't finish the process timeline with id %s because it contains steps with a status is not NEW or IN_PROCESS", timelineId));
        }
    }

    private ProcessTimeline getOrCreateNewTimelineInternal(TimelineType type, @Nullable LocalDate reportDate) {
        ProcessTimeline processTimeline;
        if (DUMMY_TIMELINE == type) {
            Objects.requireNonNull(reportDate, "reportDate parameter must be present");
            processTimeline = getOrCreateTimeline(type, reportDate);
        } else {
            throw new ProcessTimelineStepException(format("Unknown timeline type: %s", type));
        }
        return processTimeline;
    }

    private ProcessTimeline getOrCreateTimeline(TimelineType type, @Nullable LocalDate reportDate) {
        Objects.requireNonNull(reportDate, "reportDate parameter must be present");
        Optional<ProcessTimeline> paymentProcessTimelineOptional =
                processTimelineRepository.findByTypeAndReportDate(type, reportDate);
        if (paymentProcessTimelineOptional.isPresent()) {
            return paymentProcessTimelineOptional.get();
        } else {
            ProcessTimeline processTimelineForPeriod = createEmptyPaymentProcessTimelineForPeriod(type, reportDate);
            return processTimelineRepository.save(processTimelineForPeriod);
        }
    }
}
