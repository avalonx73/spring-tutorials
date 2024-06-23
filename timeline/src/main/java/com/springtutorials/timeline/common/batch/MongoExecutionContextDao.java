package com.springtutorials.timeline.common.batch;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.UpdateOptions;
import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.repository.dao.ExecutionContextDao;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.util.Assert;
import org.springframework.util.NumberUtils;

import javax.annotation.PostConstruct;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Collection;
import java.util.Map;

@Repository
public class MongoExecutionContextDao extends AbstractMongoDao implements ExecutionContextDao {
    private static final Logger LOG = LoggerFactory.getLogger(MongoExecutionContextDao.class);

    public MongoExecutionContextDao(MongoTemplate mongoTemplate) {
        super(mongoTemplate);
    }

    @PostConstruct
    public void init() {
        getCollection().createIndex(executionContextIndex());
    }

    @Override
    public ExecutionContext getExecutionContext(JobExecution jobExecution) {
        return getExecutionContext(JOB_EXECUTION_ID_KEY, jobExecution.getId());
    }

    @Override
    public ExecutionContext getExecutionContext(StepExecution stepExecution) {
        return getExecutionContext(STEP_EXECUTION_ID_KEY, stepExecution.getId());
    }

    @Override
    public void saveExecutionContext(JobExecution jobExecution) {
        saveOrUpdateExecutionContext(JOB_EXECUTION_ID_KEY,
                jobExecution.getId(), jobExecution.getExecutionContext());
    }

    @Override
    public void saveExecutionContext(StepExecution stepExecution) {
        saveOrUpdateExecutionContext(STEP_EXECUTION_ID_KEY,
                stepExecution.getId(), stepExecution.getExecutionContext());
    }

    @Override
    public void updateExecutionContext(JobExecution jobExecution) {
        saveOrUpdateExecutionContext(JOB_EXECUTION_ID_KEY,
                jobExecution.getId(), jobExecution.getExecutionContext());
    }

    @Override
    public void updateExecutionContext(StepExecution stepExecution) {
        saveOrUpdateExecutionContext(STEP_EXECUTION_ID_KEY,
                stepExecution.getId(), stepExecution.getExecutionContext());
    }

    @Override
    public void saveExecutionContexts(Collection<StepExecution> stepExecutions) {
        Assert.notNull(stepExecutions, "Attempt to save a null collection of step executions");
        for (StepExecution stepExecution : stepExecutions) {
            saveExecutionContext(stepExecution);
            saveExecutionContext(stepExecution.getJobExecution());
        }
    }

    private Document executionContextIndex() {
        return new Document()
                .append(STEP_EXECUTION_ID_KEY, 1)
                .append(JOB_EXECUTION_ID_KEY, 1);
    }

    private void saveOrUpdateExecutionContext(String executionIdKey,
                                              Long executionId, ExecutionContext executionContext) {
        Assert.notNull(executionId, "ExecutionId must not be null.");
        Assert.notNull(executionContext, "The ExecutionContext must not be null.");

        var document = new Document(executionIdKey, executionId);
        for (Map.Entry<String, Object> entry : executionContext.entrySet()) {
            Object value = entry.getValue();
            String key = entry.getKey();
            document.put(key.replace(DOT_STRING, DOT_ESCAPE_STRING), value);
            if (value instanceof BigDecimal || value instanceof BigInteger) {
                document.put(key + TYPE_SUFFIX, value.getClass().getName());
            }
        }
        getCollection().updateOne(new Document(executionIdKey, executionId),
                new Document("$set", document), new UpdateOptions().upsert(true));
    }

    @SuppressWarnings({"unchecked"})
    private ExecutionContext getExecutionContext(String executionIdKey, Long executionId) {
        Assert.notNull(executionId, "ExecutionId must not be null.");
        Document result = getCollection().find(new Document(executionIdKey, executionId)).first();
        var executionContext = new ExecutionContext();
        if (result != null) {
            result.remove(executionIdKey);
            removeSystemFields(result);
            for (Map.Entry<String, Object> entry : result.entrySet()) {
                String ctxParamKey = entry.getKey();
                Object ctxParamValue = entry.getValue();
                String type = (String) result.get(ctxParamKey + TYPE_SUFFIX);
                if (type != null && Number.class.isAssignableFrom(ctxParamValue.getClass())) {
                    try {
                        ctxParamValue = NumberUtils.convertNumberToTargetClass(
                                (Number) ctxParamValue,
                                (Class<? extends Number>) Class.forName(type));
                    } catch (Exception e) {
                        LOG.warn("Failed to convert {} to {}", ctxParamKey, type);
                    }
                }
                //Mongo db does not allow ctxParamKey name with "." character.
                executionContext.put(ctxParamKey.replace(DOT_ESCAPE_STRING, DOT_STRING), ctxParamValue);
            }
        }
        return executionContext;
    }

    protected MongoCollection<Document> getCollection() {
        return getMongoTemplate().getCollection(ExecutionContext.class.getSimpleName());
    }
}

