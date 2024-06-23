package com.springtutorials.timeline.common.batch;

import org.springframework.batch.core.repository.dao.ExecutionContextDao;
import org.springframework.batch.core.repository.dao.JobExecutionDao;
import org.springframework.batch.core.repository.dao.JobInstanceDao;
import org.springframework.batch.core.repository.dao.StepExecutionDao;
import org.springframework.batch.core.repository.support.AbstractJobRepositoryFactoryBean;
import org.springframework.beans.factory.InitializingBean;

public class MongoJobRepositoryFactoryBean extends AbstractJobRepositoryFactoryBean implements InitializingBean {

    /**
     * To store sprinbatch metadata in MongoDB this should hold an instance of {@link MongoExecutionContextDao}
     */
    private ExecutionContextDao executionContextDao;

    /**
     * To store sprinbatch metadata in MongoDB this should hold an instance of {@link MongoJobExecutionDao}
     */
    private JobExecutionDao jobExecutionDao;

    /**
     * To store sprinbatch metadata in MongoDB this should hold an instance of {@link MongoJobInstanceDao}
     */
    private JobInstanceDao jobInstanceDao;

    /**
     * To store sprinbatch metadata in MongoDB this should hold an instance of {@link MongoStepExecutionDao}
     */
    private StepExecutionDao stepExecutionDao;

    /**
     * Should hold an instance of {@link MongoExecutionContextDao}
     */
    public void setExecutionContextDao(ExecutionContextDao executionContextDao) {
        this.executionContextDao = executionContextDao;
    }

    /**
     * Should hold an instance of {@link MongoJobExecutionDao}
     */
    public void setJobExecutionDao(JobExecutionDao jobExecutionDao) {
        this.jobExecutionDao = jobExecutionDao;
    }

    /**
     * Should hold an instance of {@link MongoJobInstanceDao}
     */
    public void setJobInstanceDao(JobInstanceDao jobInstanceDao) {
        this.jobInstanceDao = jobInstanceDao;
    }

    /**
     * Should hold an instance of {@link MongoStepExecutionDao}
     */
    public void setStepExecutionDao(StepExecutionDao stepExecutionDao) {
        this.stepExecutionDao = stepExecutionDao;
    }

    @Override
    protected JobInstanceDao createJobInstanceDao() {
        return jobInstanceDao;
    }

    @Override
    protected JobExecutionDao createJobExecutionDao() {
        return jobExecutionDao;
    }

    @Override
    protected StepExecutionDao createStepExecutionDao() {
        return stepExecutionDao;
    }

    @Override
    protected ExecutionContextDao createExecutionContextDao() {
        return executionContextDao;
    }
}

