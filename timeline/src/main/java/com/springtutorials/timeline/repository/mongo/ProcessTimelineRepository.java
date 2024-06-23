package com.springtutorials.timeline.repository.mongo;

import com.springtutorials.timeline.common.model.process.ProcessTimeline;
import com.springtutorials.timeline.common.model.process.TimelineStatus;
import com.springtutorials.timeline.common.model.process.TimelineType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface ProcessTimelineRepository extends MongoRepository<ProcessTimeline, String> {

    /**
     * Getting a paginated list of timelines by type
     *
     * @param type     timeline type
     * @param pageable pagination parameters
     * @return paginated view of all process timeline by type
     */
    Page<ProcessTimeline> findAllByType(TimelineType type, Pageable pageable);

    /**
     * Getting a paginated list of timelines by type
     *
     * @param type timeline type
     * @param statuses timeline status
     * @return paginated view of all process timeline by type
     */
    Optional<ProcessTimeline> findByTypeAndTimelineStatusNotIn(TimelineType type, Collection<TimelineStatus> statuses);

    List<ProcessTimeline> findAllByTypeAndTimelineStatusNotIn(TimelineType type, Collection<TimelineStatus> statuses);
    /**
     * Getting a specific timeline by its type and reporting date
     *
     * @param type       timeline type
     * @param reportDate reporting date
     * @return process timeline by type and report date or empty instance {@link Optional}
     */
    Optional<ProcessTimeline> findByTypeAndReportDate(TimelineType type, LocalDate reportDate);

    /**
     * Getting timelines by their type and reporting date
     *
     * @param type       timeline type
     * @param reportDate reporting date
     * @return process timelines by type and report date or empty list
     */
    List<ProcessTimeline> findAllByTypeAndReportDate(TimelineType type, LocalDate reportDate);

    /**
     * Retrieving the timeline by key date (to display the process details page)
     *
     * @param reportDate reporting date
     * @return process timeline report date or empty instance {@link Optional}
     */
    Optional<ProcessTimeline> findByReportDate(LocalDate reportDate);


    Optional<ProcessTimeline> findByTypeAndReportDate(String type, LocalDate reportDate);

    /**
     * Getting a list of timelines given type for a certain month.
     *
     * @param startOfMonth start of month
     * @param endOfMonth   end of month
     * @param type         timeline type
     * @return paginated view of all process by reporting month
     */

    @Query(value = "{$and : [{'reportDate': {$gte : ?0, $lte: ?1}}, {'type' : ?2}]}")
    List<ProcessTimeline> findAllBetweenDatesAndByType(LocalDate startOfMonth, LocalDate endOfMonth, String type);
}

