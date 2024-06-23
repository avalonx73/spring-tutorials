package com.springtutorials.timeline.common.config;

import com.springtutorials.timeline.common.batch.MongoExecutionContextDao;
import com.springtutorials.timeline.common.batch.MongoJobExecutionDao;
import com.springtutorials.timeline.common.batch.MongoJobInstanceDao;
import com.springtutorials.timeline.common.batch.MongoJobRepositoryFactoryBean;
import com.springtutorials.timeline.common.batch.MongoStepExecutionDao;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.configuration.annotation.BatchConfigurer;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.explore.JobExplorer;
import org.springframework.batch.core.explore.support.SimpleJobExplorer;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.launch.support.SimpleJobLauncher;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.integration.config.annotation.EnableBatchIntegration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.MongoTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;

import javax.annotation.PostConstruct;

@Configuration
@RequiredArgsConstructor
@EnableBatchProcessing
@EnableBatchIntegration
@ComponentScan("com.springtutorials.timeline.common.batch")
public class CommonBatchConfiguration implements BatchConfigurer {
    private final MongoTransactionManager mongoTransactionManager;
    private final MongoJobExecutionDao mongoJobExecutionDao;
    private final MongoExecutionContextDao mongoExecutionContextDao;
    private final MongoStepExecutionDao mongoStepExecutionDao;
    private final MongoJobInstanceDao mongoJobInstanceDao;
    private JobRepository jobRepository;
    private JobLauncher jobLauncher;
    private JobExplorer jobExplorer;

    @PostConstruct
    private void init() throws Exception {
        this.jobRepository = createJobRepository();
        this.jobLauncher = createJobLauncher();
        this.jobExplorer = createJobExplorer();
    }

    private JobRepository createJobRepository() throws Exception {
        var jobRepositoryFactoryBean = new MongoJobRepositoryFactoryBean();
        jobRepositoryFactoryBean.setJobExecutionDao(mongoJobExecutionDao);
        jobRepositoryFactoryBean.setJobInstanceDao(mongoJobInstanceDao);
        jobRepositoryFactoryBean.setStepExecutionDao(mongoStepExecutionDao);
        jobRepositoryFactoryBean.setExecutionContextDao(mongoExecutionContextDao);
        jobRepositoryFactoryBean.setTransactionManager(mongoTransactionManager);
        return jobRepositoryFactoryBean.getObject();
    }

    private JobLauncher createJobLauncher() throws Exception {
        var simpleJobLauncher = new SimpleJobLauncher();
        simpleJobLauncher.setJobRepository(getJobRepository());
        simpleJobLauncher.afterPropertiesSet();
        return simpleJobLauncher;
    }

    private JobExplorer createJobExplorer() {
        return new SimpleJobExplorer(mongoJobInstanceDao, mongoJobExecutionDao, mongoStepExecutionDao, mongoExecutionContextDao);
    }

    @Override
    public JobRepository getJobRepository() {
        return jobRepository;
    }

    @Override
    public PlatformTransactionManager getTransactionManager() {
        return mongoTransactionManager;
    }

    @Override
    public JobLauncher getJobLauncher() {
        return jobLauncher;
    }

    @Override
    public JobExplorer getJobExplorer() {
        return jobExplorer;
    }
}

