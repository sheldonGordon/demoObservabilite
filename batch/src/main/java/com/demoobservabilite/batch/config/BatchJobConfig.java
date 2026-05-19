package com.demoobservabilite.batch.config;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
@EnableBatchProcessing
public class BatchJobConfig {

    private static final Logger LOGGER = LoggerFactory.getLogger(BatchJobConfig.class);
    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${demo.backend.base-url:http://localhost:8080}")
    private String backendBaseUrl;

    @Value("${demo.backend.auth.username:demo}")
    private String backendUsername;

    @Value("${demo.backend.auth.password:demo}")
    private String backendPassword;

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
                    String token = authenticateAndGetToken();
                    FilmSummary[] films = fetchFilms(token);
                    int updatedRows = 0;

                    for (FilmSummary film : films) {
                        BigDecimal randomImdbScore = BigDecimal
                                .valueOf(ThreadLocalRandom.current().nextDouble(5.0, 10.0))
                                .setScale(1, RoundingMode.HALF_UP);

                        updateFilmScore(token, film.id(), randomImdbScore);
                        updatedRows++;
                    }

                    LOGGER.info("Mise a jour aleatoire de imdb_score via API terminee. {} films modifies.", updatedRows);
                    return RepeatStatus.FINISHED;
                }, transactionManager)
                .build();
    }

    private String authenticateAndGetToken() {
        String url = backendBaseUrl + "/api/auth/token";

        @SuppressWarnings("unchecked")
        Map<String, String> response = restTemplate.postForObject(
                url,
                Map.of("username", backendUsername, "password", backendPassword),
                Map.class);

        if (response == null || response.get("token") == null || response.get("token").isBlank()) {
            throw new IllegalStateException("Impossible de recuperer le token JWT depuis l'API backend.");
        }
        return response.get("token");
    }

    private FilmSummary[] fetchFilms(String token) {
        HttpEntity<Void> requestEntity = new HttpEntity<>(createAuthHeaders(token));
        ResponseEntity<FilmSummary[]> response = restTemplate.exchange(
                backendBaseUrl + "/api/films",
                HttpMethod.GET,
                requestEntity,
                FilmSummary[].class);

        return response.getBody() == null ? new FilmSummary[0] : response.getBody();
    }

    private void updateFilmScore(String token, Long filmId, BigDecimal imdbScore) {
        HttpEntity<Map<String, BigDecimal>> requestEntity = new HttpEntity<>(
                Map.of("imdbScore", imdbScore),
                createAuthHeaders(token));

        restTemplate.exchange(
                backendBaseUrl + "/api/films/" + filmId,
                HttpMethod.PUT,
                requestEntity,
                Void.class);
    }

    private HttpHeaders createAuthHeaders(String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        headers.setContentType(MediaType.APPLICATION_JSON);
        return headers;
    }

    private record FilmSummary(Long id, String title, Integer release_year, String genre) {
    }
}

