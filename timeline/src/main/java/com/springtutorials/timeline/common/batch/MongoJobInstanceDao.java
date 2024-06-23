package com.springtutorials.timeline.common.batch;

import com.mongodb.BasicDBObject;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import org.bson.Document;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobInstance;
import org.springframework.batch.core.JobParameter;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.launch.NoSuchJobException;
import org.springframework.batch.core.repository.dao.JobInstanceDao;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.util.Assert;

import javax.annotation.PostConstruct;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

@Repository
public class MongoJobInstanceDao extends AbstractMongoDao implements JobInstanceDao {

    public MongoJobInstanceDao(MongoTemplate mongoTemplate) {
        super(mongoTemplate);
    }

    @PostConstruct
    public void init() {
        getCollection().createIndex(jobInstanceIdObj(1L));
    }

    @Override
    public JobInstance createJobInstance(String jobName, final JobParameters jobParameters) {
        Assert.notNull(jobName, "Job name must not be null.");
        Assert.notNull(jobParameters, "JobParameters must not be null.");

        Assert.state(getJobInstance(jobName, jobParameters) == null,
                "JobInstance must not already exist");

        Long jobId = getNextId(JobInstance.class.getSimpleName());

        JobInstance jobInstance = new JobInstance(jobId, jobName);

        jobInstance.incrementVersion();

        Map<String, JobParameter> jobParams = jobParameters.getParameters();
        Map<String, Object> paramMap = new HashMap<>(jobParams.size());
        for (Map.Entry<String, JobParameter> entry : jobParams.entrySet()) {
            paramMap.put(entry.getKey().replace(DOT_STRING, DOT_ESCAPE_STRING), entry.getValue().getValue());
        }

        getCollection().insertOne(new Document()
                .append(JOB_INSTANCE_ID_KEY, jobId)
                .append(JOB_NAME_KEY, jobName)
                .append(JOB_KEY_KEY, createJobKey(jobParameters))
                .append(VERSION_KEY, jobInstance.getVersion())
                .append(JOB_PARAMETERS_KEY, new Document(paramMap)));
        return jobInstance;
    }

    @Override
    public JobInstance getJobInstance(String jobName, JobParameters jobParameters) {
        Assert.notNull(jobName, "Job name must not be null.");
        Assert.notNull(jobParameters, "JobParameters must not be null.");

        String jobKey = createJobKey(jobParameters);

        return mapJobInstance(getCollection().find(new Document()
                .append(JOB_NAME_KEY, jobName)
                .append(JOB_KEY_KEY, jobKey)).first());
    }

    @Override
    public JobInstance getJobInstance(Long instanceId) {
        return mapJobInstance(getCollection()
                .find(jobInstanceIdObj(instanceId))
                .first());
    }

    @Override
    public JobInstance getJobInstance(JobExecution jobExecution) {
        Document instanceId = getMongoTemplate().getCollection(JobExecution.class.getSimpleName())
                .find(jobExecutionIdObj(jobExecution.getId()))
                .projection(jobInstanceIdObj(1L))
                .first();
        if (instanceId != null) {
            removeSystemFields(instanceId);
            return mapJobInstance(getCollection().find(instanceId).first());
        }
        return null;
    }

    @Override
    public List<JobInstance> getJobInstances(String jobName, int start, int count) {
        return mapJobInstances(getCollection()
                .find(new Document(JOB_NAME_KEY, jobName))
                .sort(jobInstanceIdObj(-1L))
                .skip(start)
                .limit(count)
                .cursor());
    }

    @Override
    public JobInstance getLastJobInstance(String jobName) {
        return mapJobInstance(getCollection()
                .find(new Document(JOB_NAME_KEY, jobName))
                .sort(jobInstanceIdObj(-1))
                .limit(1)
                .first());
    }

    @Override
    protected MongoCollection<Document> getCollection() {
        return getMongoTemplate().getCollection(JobInstance.class.getSimpleName());
    }

    @Override
    public List<JobInstance> findJobInstancesByName(String jobName, int start, int count) {
        List<JobInstance> result = new ArrayList<>();
        List<JobInstance> jobInstances = mapJobInstances(getCollection().find(
                new Document(JOB_NAME_KEY, jobName)).sort(
                jobInstanceIdObj(-1L)).cursor());
        for (JobInstance instanceEntry : jobInstances) {
            String key = instanceEntry.getJobName();
            var curJobName = key.substring(0, key.lastIndexOf("|"));

            if (curJobName.equals(jobName)) {
                result.add(instanceEntry);
            }
        }
        return result;
    }

    @Override
    public int getJobInstanceCount(String jobName) throws NoSuchJobException {
        int count = 0;
        List<JobInstance> jobInstances = mapJobInstances(getCollection().find(
                new BasicDBObject(JOB_NAME_KEY, jobName)).sort(
                jobInstanceIdObj(-1L)).cursor());
        for (JobInstance instanceEntry : jobInstances) {
            String key = instanceEntry.getJobName();
            String curJobName = key.substring(0, key.lastIndexOf("|"));

            if (curJobName.equals(jobName)) {
                count++;
            }
        }

        if (count == 0) {
            throw new NoSuchJobException("No job instances for job name " + jobName + " were found");
        } else {
            return count;
        }
    }

    public List<String> getJobNames() {
        List<String> names = new ArrayList<>();
        Iterator<String> namesIt = getCollection().distinct(JOB_NAME_KEY, String.class).iterator();
        while (namesIt.hasNext()) {
            names.add(namesIt.next());
        }
        Collections.sort(names);
        return names;
    }

    protected String createJobKey(JobParameters jobParameters) {

        Map<String, JobParameter> props = jobParameters.getParameters();
        StringBuilder stringBuilder = new StringBuilder();
        List<String> keys = new ArrayList<>(props.keySet());
        Collections.sort(keys);
        for (String key : keys) {
            stringBuilder.append(key).append("=").append(props.get(key).toString()).append(";");
        }

        MessageDigest digest;
        try {
            digest = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(
                    "MD5 algorithm not available.  Fatal (should be in the JDK).");
        }

        byte[] bytes = digest.digest(stringBuilder.toString().getBytes(StandardCharsets.UTF_8));
        return String.format("%032x", new BigInteger(1, bytes));
    }

    private List<JobInstance> mapJobInstances(MongoCursor<Document> dbCursor) {
        List<JobInstance> results = new ArrayList<>();
        try (dbCursor) {
            while (dbCursor.hasNext()) {
                results.add(mapJobInstance(dbCursor.next()));
            }
        }
        return results;
    }

    private JobInstance mapJobInstance(Document document) {
        JobInstance jobInstance = null;
        if (document != null) {
            Long id = document.getLong(JOB_INSTANCE_ID_KEY);

            jobInstance = new JobInstance(id, document.getString(JOB_NAME_KEY)); // should always be at version=0 because they never get updated
            jobInstance.incrementVersion();
        }
        return jobInstance;
    }
}

