package com.springtutorials.timeline.service.process;

import com.springtutorials.timeline.common.model.process.ProcessTimeline;
import com.springtutorials.timeline.common.model.process.TimelineType;
import com.springtutorials.timeline.dto.rest.process.ProcessTimelineDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.time.YearMonth;

public interface ProcessTimelineService {
    /**
     * Create a new timeline or get existing
     *
     * @param type       of timeline type
     * @param reportDate reporting date
     * @return created or already existing timeline dto
     */
    ProcessTimelineDto findOrCreateCurrentTimeline(TimelineType type, LocalDate reportDate);

    /**
     * Create a new timeline or get existing
     *
     * @param type       of timeline type
     * @param reportDate reporting date
     * @return created or already existing timeline
     */
    ProcessTimeline getOrCreateCurrentTimeline(TimelineType type, LocalDate reportDate);

    /**
     * Timeline search by reporting period
     *
     * @param reportDate reporting date
     * @return found timeline
     */
    ProcessTimelineDto findByReportDate(LocalDate reportDate);

    /**
     * Timeline search by type and reporting period
     *
     * @param reportDate reporting date
     * @return found timeline
     */
    ProcessTimelineDto findByTypeAndReportDate(TimelineType type, LocalDate reportDate);

    /**
     * Search all Timeline by reporting year
     *
     * @param type        of timeline type
     * @param reportMonth reporting month
     * @return result page
     */

    Page<ProcessTimelineDto> findAllByReportMonthAndType(YearMonth reportMonth, TimelineType type, Pageable pageable);

    /**
     * Search all Timeline with a given type
     *
     * @param type     the type of Timeline to look for
     * @param pageable pagination options
     * @return result Page
     */
    Page<ProcessTimelineDto> findAllByType(TimelineType type, Pageable pageable);

    /**
     * Find timeline by Id
     *
     * @param id Timeline ID
     * @return desired timeline
     */
    ProcessTimelineDto findById(String id);

    /**
     * Delete timeline by id
     *
     * @param timelineId Timeline ID
     * @return void
     */
    void delete(String timelineId);

    /**
     * Finish timeline by id
     *
     * @param timelineId Timeline ID
     * @return void
     */
    void finishTimeline(String timelineId);
}

