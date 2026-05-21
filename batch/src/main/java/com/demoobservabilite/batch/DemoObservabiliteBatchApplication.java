package com.demoobservabilite.batch;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class DemoObservabiliteBatchApplication {

    private static final Logger LOGGER = LoggerFactory.getLogger(DemoObservabiliteBatchApplication.class);

    public static void main(String[] args) {
        SpringApplication.run(DemoObservabiliteBatchApplication.class, args);
        LOGGER.info("Application batch demoObservabilite demarree avec succes");
    }

    @Bean
    public CommandLineRunner runDemoJob(JobLauncher jobLauncher, Job demoJob) {
        return args -> {
            JobParameters parameters = new JobParametersBuilder()
                    .addLong("run.id", System.currentTimeMillis())
                    .toJobParameters();

            LOGGER.info("Declenchement manuel du job '{}'", demoJob.getName());
            jobLauncher.run(demoJob, parameters);
        };
    }
}

