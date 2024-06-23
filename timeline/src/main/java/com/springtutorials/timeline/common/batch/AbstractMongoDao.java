package com.springtutorials.timeline.common.batch;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.UpdateOptions;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.bson.Document;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobInstance;
import org.springframework.batch.core.JobParameter;
import org.springframework.batch.core.JobParameters;
import org.springframework.data.mongodb.core.MongoTemplate;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Getter
@RequiredArgsConstructor
public abstract class AbstractMongoDao {
    public static final String VERSION_KEY = "version";
    public static final String START_TIME_KEY = "startTime";
    public static final String END_TIME_KEY = "endTime";
    public static final String EXIT_CODE_KEY = "exitCode";
    public static final String EXIT_MESSAGE_KEY = "exitMessage";
    public static final String LAST_UPDATED_KEY = "lastUpdated";
    public static final String STATUS_KEY = "status";
    public static final String SEQUENCES_COLLECTION_NAME = "Sequences";
    public static final String ID_KEY = "_id";
    public static final String NS_KEY = "_ns";
    public static final String DOT_ESCAPE_STRING = "{dot}";
    public static final String DOT_STRING = ".";

    // Job Constants
    public static final String JOB_NAME_KEY = "jobName";
    public static final String JOB_INSTANCE_ID_KEY = "jobInstanceId";
    public static final String JOB_KEY_KEY = "jobKey";
    public static final String JOB_PARAMETERS_KEY = "jobParameters";

    // Job Execution Constants
    public static final String JOB_EXECUTION_ID_KEY = "jobExecutionId";
    public static final String CREATE_TIME_KEY = "createTime";

    // Job Execution Contexts Constants
    public static final String STEP_EXECUTION_ID_KEY = "stepExecutionId";
    public static final String TYPE_SUFFIX = "_TYPE";

    // Step Execution Constants
    public static final String STEP_NAME_KEY = "stepName";
    public static final String COMMIT_COUNT_KEY = "commitCount";
    public static final String READ_COUNT_KEY = "readCount";
    public static final String FILTER_COUT_KEY = "filterCout";
    public static final String WRITE_COUNT_KEY = "writeCount";
    public static final String READ_SKIP_COUNT_KEY = "readSkipCount";
    public static final String WRITE_SKIP_COUNT_KEY = "writeSkipCount";
    public static final String PROCESS_SKIP_COUT_KEY = "processSkipCout";
    public static final String ROLLBACK_COUNT_KEY = "rollbackCount";

    private final MongoTemplate mongoTemplate;

    protected abstract MongoCollection<Document> getCollection();

    protected Long getNextId(String name) {
        MongoCollection<Document> collection = mongoTemplate.getDb().getCollection(SEQUENCES_COLLECTION_NAME);
        var sequence = new Document("name", name);
        collection.updateOne(sequence, new Document("$inc", new Document("value", 1L)), new UpdateOptions().upsert(true));
        return collection.find(sequence).first().getLong("value");
    }

    protected void removeSystemFields(Document doc) {
        doc.remove(ID_KEY);
        doc.remove(NS_KEY);
    }

    protected Document jobInstanceIdObj(long id) {
        return new Document(JOB_INSTANCE_ID_KEY, id);
    }

    protected Document jobExecutionIdObj(long id) {
        return new Document(JOB_EXECUTION_ID_KEY, id);
    }

    @SuppressWarnings({"unchecked"})
    protected JobParameters getJobParameters(Long jobInstanceId, MongoTemplate mongoTemplate) {
        Document jobParamObj = mongoTemplate
                .getCollection(JobInstance.class.getSimpleName())
                .find(new Document(jobInstanceIdObj(jobInstanceId))).first();

        if (jobParamObj != null && jobParamObj.get(JOB_PARAMETERS_KEY) != null) {

            Map<String, ?> jobParamsMap = (Map<String, ?>) jobParamObj.get(JOB_PARAMETERS_KEY);

            Map<String, JobParameter> map = new HashMap<>(jobParamsMap.size());
            for (Map.Entry<String, ?> entry : jobParamsMap.entrySet()) {
                Object param = entry.getValue();
                String key = entry.getKey().replace(DOT_ESCAPE_STRING, DOT_STRING);
                if (param instanceof String) {
                    map.put(key, new JobParameter((String) param));
                } else if (param instanceof Long) {
                    map.put(key, new JobParameter((Long) param));
                } else if (param instanceof Double) {
                    map.put(key, new JobParameter((Double) param));
                } else if (param instanceof Date) {
                    map.put(key, new JobParameter((Date) param));
                } else {
                    map.put(key, null);
                }
            }
            return new JobParameters(map);
        }
        return null;
    }

    public JobExecution mapJobExecution(JobInstance jobInstance,
                                        Document dbObject) {
        if (dbObject == null) {
            return null;
        }
        Long id = (Long) dbObject.get(JOB_EXECUTION_ID_KEY);
        JobExecution jobExecution;

        if (jobInstance == null) {
            Long jobInstanceId = (Long) dbObject.get(JOB_INSTANCE_ID_KEY);
            var jobParameters = getJobParameters(jobInstanceId, mongoTemplate);
            jobExecution = new JobExecution(id, jobParameters);
        } else {
            var jobParameters = getJobParameters(jobInstance.getId(), mongoTemplate);
            jobExecution = new JobExecution(jobInstance, id, jobParameters, null); //NOSONAR
        }
        jobExecution.setStartTime((Date) dbObject.get(START_TIME_KEY));
        jobExecution.setEndTime((Date) dbObject.get(END_TIME_KEY));
        jobExecution.setStatus(BatchStatus.valueOf((String) dbObject
                .get(STATUS_KEY)));
        jobExecution.setExitStatus(new ExitStatus(((String) dbObject
                .get(EXIT_CODE_KEY)), (String) dbObject.get(EXIT_MESSAGE_KEY)));
        jobExecution.setCreateTime((Date) dbObject.get(CREATE_TIME_KEY));
        jobExecution.setLastUpdated((Date) dbObject.get(LAST_UPDATED_KEY));
        jobExecution.setVersion((Integer) dbObject.get(VERSION_KEY));

        return jobExecution;
    }
}
