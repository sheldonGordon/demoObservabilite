package com.demoobservabilite.batch;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;

@SpringBootApplication
public class DemoObservabiliteBatchApplication {

    private static final Logger LOGGER = LoggerFactory.getLogger(DemoObservabiliteBatchApplication.class);

    public static void main(String[] args) {
        ApplicationContext context = SpringApplication.run(DemoObservabiliteBatchApplication.class, args);
        LOGGER.info("Application batch demoObservabilite demarree avec succes");
    }
}

