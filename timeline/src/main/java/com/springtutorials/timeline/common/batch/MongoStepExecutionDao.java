package com.springtutorials.timeline.common.batch;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.result.UpdateResult;
import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobInstance;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.repository.dao.StepExecutionDao;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.util.Assert;

import javax.annotation.PostConstruct;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static org.springframework.util.Assert.notNull;

@Repository
public class MongoStepExecutionDao extends AbstractMongoDao implements StepExecutionDao {
    private static final Logger LOG = LoggerFactory.getLogger(MongoStepExecutionDao.class);

    public MongoStepExecutionDao(MongoTemplate mongoTemplate) {
        super(mongoTemplate);
    }

    @PostConstruct
    public void init() {
        getCollection().createIndex(new Document()
                .append(STEP_EXECUTION_ID_KEY, 1)
                .append(JOB_EXECUTION_ID_KEY, 1));
    }

    @Override
    public void saveStepExecution(StepExecution stepExecution) {
        Assert.isNull(stepExecution.getId(),
                "to-be-saved (not updated) StepExecution can't already have an id assigned");
        Assert.isNull(stepExecution.getVersion(),
                "to-be-saved (not updated) StepExecution can't already have a version assigned");

        validateStepExecution(stepExecution);

        stepExecution.setId(getNextId(StepExecution.class.getSimpleName()));
        stepExecution.incrementVersion();
        var document = toDocumentWithoutVersion(stepExecution);
        document.put(VERSION_KEY, stepExecution.getVersion());
        getCollection().insertOne(document);

    }

    @Override
    public synchronized void updateStepExecution(StepExecution stepExecution) {
        // Attempt to prevent concurrent modification errors by blocking here if
        // someone is already trying to do it.
        Integer currentVersion = stepExecution.getVersion();
        Integer newVersion = currentVersion + 1;
        var document = toDocumentWithoutVersion(stepExecution);
        document.put(VERSION_KEY, newVersion);
        UpdateResult result = getCollection().replaceOne(new Document()
                        .append(STEP_EXECUTION_ID_KEY, stepExecution.getId())
                        .append(VERSION_KEY, currentVersion),
                document);

        // Avoid concurrent modifications...
        if (result.getMatchedCount() == 0 && result.getModifiedCount() == 0) {
            LOG.error("Update returned status {}", result);
            Document existingStepExecution = getCollection().find(stepExecutionIdObj(stepExecution.getId())).projection(new Document(VERSION_KEY, 1)).first();
            if (existingStepExecution == null) {
                throw new IllegalArgumentException("Can't update this stepExecution, it was never saved.");
            }
            Integer curentVersion = ((Integer) existingStepExecution.get(VERSION_KEY));
            throw new OptimisticLockingFailureException("Attempt to update job execution id="
                    + stepExecution.getId() + " with wrong version (" + currentVersion
                    + "), where current version is " + curentVersion);
        }
        stepExecution.incrementVersion();
    }

    @Override
    public StepExecution getStepExecution(JobExecution jobExecution, Long stepExecutionId) {
        return mapStepExecution(getCollection().find(new Document()
                .append(STEP_EXECUTION_ID_KEY, stepExecutionId)
                .append(JOB_EXECUTION_ID_KEY, jobExecution.getId())).first(), jobExecution);
    }

    @Override
    public void addStepExecutions(JobExecution jobExecution) {
        try (MongoCursor<Document> stepsCursor = getCollection().find(jobExecutionIdObj(jobExecution.getId()))
                .sort(stepExecutionIdObj(1L)).cursor()) {
            while (stepsCursor.hasNext()) {
                Document stepObject = stepsCursor.next();

                mapStepExecution(stepObject, jobExecution);
            }
        }
    }

    @Override
    protected MongoCollection<Document> getCollection() {
        return getMongoTemplate().getCollection(StepExecution.class.getSimpleName());
    }

    @Override
    public void saveStepExecutions(Collection<StepExecution> stepExecutions) {
        Assert.notNull(stepExecutions, "Attempt to save an null collect of step executions");
        for (StepExecution stepExecution : stepExecutions) {
            saveStepExecution(stepExecution);
        }
    }

    @Override
    public StepExecution getLastStepExecution(JobInstance jobInstance, String stepName) {

        Map<Long, Document> jobExecutionsById = getExecutionsForJobInstance(jobInstance);

        if (!jobExecutionsById.isEmpty()) {
            Document foundStepExecution = getCollection().find(new Document()
                            .append(STEP_NAME_KEY, stepName)
                            .append(JOB_EXECUTION_ID_KEY, new Document()
                                    .append("$in", jobExecutionsById.keySet())))
                    .sort(new Document()
                            .append(START_TIME_KEY, -1).append(STEP_EXECUTION_ID_KEY, -1)).first();
            if (foundStepExecution != null) {
                return mapStepExecution(foundStepExecution, mapJobExecution(jobInstance,
                        jobExecutionsById.get(foundStepExecution.get(JOB_EXECUTION_ID_KEY))));
            }
        }
        return null;
    }

    private Map<Long, Document> getExecutionsForJobInstance(JobInstance jobInstance) {
        Map<Long, Document> jobExecutionsById = new HashMap<>();
        try (MongoCursor<Document> jobExecutionCursor = jobExecutionByJobInstanceCursor(jobInstance)) {
            while (jobExecutionCursor.hasNext()) {
                Document jobExecution = jobExecutionCursor.next();
                jobExecutionsById.put((Long) jobExecution.get(JOB_EXECUTION_ID_KEY), jobExecution);
            }
        }
        return jobExecutionsById;
    }

    private MongoCursor<Document> jobExecutionByJobInstanceCursor(JobInstance jobInstance) {
        return getMongoTemplate().getCollection(JobExecution.class.getSimpleName())
                .find(new Document()
                        .append(JOB_INSTANCE_ID_KEY, jobInstance.getInstanceId()))
                .cursor();
    }

    @Override
    public int countStepExecutions(JobInstance jobInstance, String stepName) {
        Map<Long, Document> jobExecutionsById = getExecutionsForJobInstance(jobInstance);

        if (!jobExecutionsById.isEmpty()) {
            return (int) getCollection().countDocuments(new Document()
                    .append(STEP_NAME_KEY, stepName)
                    .append(JOB_EXECUTION_ID_KEY, new Document()
                            .append("$in", jobExecutionsById.keySet())));
        }
        return 0;
    }

    private Document toDocumentWithoutVersion(StepExecution stepExecution) {
        return new Document()
                .append(STEP_EXECUTION_ID_KEY, stepExecution.getId())
                .append(STEP_NAME_KEY, stepExecution.getStepName())
                .append(JOB_EXECUTION_ID_KEY, stepExecution.getJobExecutionId())
                .append(START_TIME_KEY, stepExecution.getStartTime())
                .append(END_TIME_KEY, stepExecution.getEndTime())
                .append(STATUS_KEY, stepExecution.getStatus().toString())
                .append(COMMIT_COUNT_KEY, stepExecution.getCommitCount())
                .append(READ_COUNT_KEY, stepExecution.getReadCount())
                .append(FILTER_COUT_KEY, stepExecution.getFilterCount())
                .append(WRITE_COUNT_KEY, stepExecution.getWriteCount())
                .append(EXIT_CODE_KEY, stepExecution.getExitStatus().getExitCode())
                .append(EXIT_MESSAGE_KEY, stepExecution.getExitStatus().getExitDescription())
                .append(READ_SKIP_COUNT_KEY, stepExecution.getReadSkipCount())
                .append(WRITE_SKIP_COUNT_KEY, stepExecution.getWriteSkipCount())
                .append(PROCESS_SKIP_COUT_KEY, stepExecution.getProcessSkipCount())
                .append(ROLLBACK_COUNT_KEY, stepExecution.getRollbackCount())
                .append(LAST_UPDATED_KEY, stepExecution.getLastUpdated());
    }


    static Document stepExecutionIdObj(Long id) {
        return new Document(STEP_EXECUTION_ID_KEY, id);
    }


    private StepExecution mapStepExecution(Document object, JobExecution jobExecution) {
        if (object == null) {
            return null;
        }
        var stepExecution = new StepExecution((String) object.get(STEP_NAME_KEY), jobExecution, ((Long) object.get(STEP_EXECUTION_ID_KEY)));
        stepExecution.setStartTime((Date) object.get(START_TIME_KEY));
        stepExecution.setEndTime((Date) object.get(END_TIME_KEY));
        stepExecution.setStatus(BatchStatus.valueOf((String) object.get(STATUS_KEY)));
        stepExecution.setCommitCount((Integer) object.get(COMMIT_COUNT_KEY));
        stepExecution.setReadCount((Integer) object.get(READ_COUNT_KEY));
        stepExecution.setFilterCount((Integer) object.get(FILTER_COUT_KEY));
        stepExecution.setWriteCount((Integer) object.get(WRITE_COUNT_KEY));
        stepExecution.setExitStatus(new ExitStatus((String) object.get(EXIT_CODE_KEY), ((String) object.get(EXIT_MESSAGE_KEY))));
        stepExecution.setReadSkipCount((Integer) object.get(READ_SKIP_COUNT_KEY));
        stepExecution.setWriteSkipCount((Integer) object.get(WRITE_SKIP_COUNT_KEY));
        stepExecution.setProcessSkipCount((Integer) object.get(PROCESS_SKIP_COUT_KEY));
        stepExecution.setRollbackCount((Integer) object.get(ROLLBACK_COUNT_KEY));
        stepExecution.setLastUpdated((Date) object.get(LAST_UPDATED_KEY));
        stepExecution.setVersion((Integer) object.get(VERSION_KEY));
        return stepExecution;
    }

    private void validateStepExecution(StepExecution stepExecution) {
        notNull(stepExecution, "StepExecution cannot be null");
        notNull(stepExecution.getStepName(), "StepExecution step name cannot be null.");
        notNull(stepExecution.getStartTime(), "StepExecution start time cannot be null.");
        notNull(stepExecution.getStatus(), "StepExecution status cannot be null.");
    }
}
