package com.demoobservabilite.batch.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableBatchProcessing
public class BatchJobConfig {

    private static final Logger LOGGER = LoggerFactory.getLogger(BatchJobConfig.class);

    @Bean
    public Job demoJob(JobBuilderFactory jobBuilderFactory, Step demoStep) {
        return jobBuilderFactory.get("demoJob")
                .start(demoStep)
                .build();
    }

    @Bean
    public Step demoStep(StepBuilderFactory stepBuilderFactory) {
        return stepBuilderFactory.get("demoStep")
                .tasklet((contribution, chunkContext) -> {
                    LOGGER.info("Exécution du batch de démonstration terminée avec succès.");
                    return RepeatStatus.FINISHED;
                })
                .build();
    }
}

