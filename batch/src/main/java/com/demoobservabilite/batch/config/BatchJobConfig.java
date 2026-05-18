package com.demoobservabilite.batch.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobRepository;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
@EnableBatchProcessing
public class BatchJobConfig {

    private static final Logger LOGGER = LoggerFactory.getLogger(BatchJobConfig.class);

    @Bean
    public Job demoJob(JobRepository jobRepository, Step demoStep) {
        return new JobBuilder("demoJob", jobRepository)
                .start(demoStep)
                .build();
    }

    @Bean
    public Step demoStep(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
        return new StepBuilder("demoStep", jobRepository)
                .tasklet((contribution, chunkContext) -> {
                    LOGGER.info("Execution du batch de demonstration terminee avec succes.");
                    return RepeatStatus.FINISHED;
                }, transactionManager)
                .build();
    }
}

