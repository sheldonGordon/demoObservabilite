package com.demoobservabilite.batch.config;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
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
    private static final String TRACE_ID_HEADER = "X-Trace-Id";
    private static final String SESSION_ID_HEADER = "X-Session-Id";
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
                    String traceId = generateTraceId();
                    String sessionId = "batch-" + UUID.randomUUID();
                    MDC.put("trace_id", traceId);
                    MDC.put("frontend_session_id", sessionId);

                    try {
                        LOGGER.info("Demarrage du batch de mise a jour des scores IMDB");
                        LOGGER.debug("TraceId: {}, SessionId: {}", traceId, sessionId);
                        
                        LOGGER.info("Etape 1: Authentification aupres du backend...");
                        String token = authenticateAndGetToken(traceId, sessionId);
                        LOGGER.info("Authentification reussie, token obtenu");
                        
                        LOGGER.info("Etape 2: Recuperation de la liste des films...");
                        FilmSummary[] films = fetchFilms(token, traceId, sessionId);
                        LOGGER.info("Recuperation reussie, {} films trouves", films.length);
                        
                        int updatedRows = 0;

                        LOGGER.info("Etape 3: Mise a jour des scores IMDB...");
                        for (FilmSummary film : films) {
                            BigDecimal randomImdbScore = BigDecimal
                                    .valueOf(ThreadLocalRandom.current().nextDouble(5.0, 10.0))
                                    .setScale(1, RoundingMode.HALF_UP);

                            LOGGER.debug("Mise a jour du film {} avec le score {}", film.id(), randomImdbScore);
                            updateFilmScore(token, film.id(), randomImdbScore, traceId, sessionId);
                            updatedRows++;
                        }

                        LOGGER.info("Mise a jour aleatoire de imdb_score via API terminee. {} films modifies.", updatedRows);
                        LOGGER.info("Batch termine avec succes");
                    } catch (Exception e) {
                        LOGGER.error("Erreur lors de l'execution du batch: {}", e.getMessage(), e);
                        throw e;
                    } finally {
                        MDC.remove("trace_id");
                        MDC.remove("frontend_session_id");
                    }
                    return RepeatStatus.FINISHED;
                }, transactionManager)
                .build();
    }

    private String authenticateAndGetToken(String traceId, String sessionId) {
        String url = backendBaseUrl + "/api/auth/token";

        HttpEntity<Map<String, String>> requestEntity = new HttpEntity<>(
                Map.of("username", backendUsername, "password", backendPassword),
                createCorrelationHeaders(traceId, sessionId));

        try {
            @SuppressWarnings("unchecked")
            ResponseEntity<Map> response = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    requestEntity,
                    Map.class);

            @SuppressWarnings("unchecked")
            Map<String, String> body = response.getBody();

            if (body == null || body.get("token") == null || body.get("token").isBlank()) {
                throw new IllegalStateException("Impossible de recuperer le token JWT depuis l'API backend.");
            }
            LOGGER.debug("Token JWT obtenu avec succes");
            return body.get("token");
        } catch (Exception e) {
            LOGGER.error("Erreur lors de l'authentification aupres de {}: {}", url, e.getMessage(), e);
            throw e;
        }
    }

    private FilmSummary[] fetchFilms(String token, String traceId, String sessionId) {
        try {
            HttpEntity<Void> requestEntity = new HttpEntity<>(createAuthHeaders(token, traceId, sessionId));
            ResponseEntity<FilmSummary[]> response = restTemplate.exchange(
                    backendBaseUrl + "/api/films",
                    HttpMethod.GET,
                    requestEntity,
                    FilmSummary[].class);

            FilmSummary[] result = response.getBody() == null ? new FilmSummary[0] : response.getBody();
            LOGGER.debug("Films recuperes: {}", (Object[]) result);
            return result;
        } catch (Exception e) {
            LOGGER.error("Erreur lors de la recuperation des films: {}", e.getMessage(), e);
            throw e;
        }
    }

    private void updateFilmScore(String token, Long filmId, BigDecimal imdbScore, String traceId, String sessionId) {
        try {
            HttpEntity<Map<String, BigDecimal>> requestEntity = new HttpEntity<>(
                    Map.of("imdbScore", imdbScore),
                    createAuthHeaders(token, traceId, sessionId));

            restTemplate.exchange(
                    backendBaseUrl + "/api/films/" + filmId,
                    HttpMethod.PUT,
                    requestEntity,
                    Void.class);
            LOGGER.debug("Film {} mis a jour avec le score {}", filmId, imdbScore);
        } catch (Exception e) {
            LOGGER.error("Erreur lors de la mise a jour du film {}: {}", filmId, e.getMessage(), e);
            throw e;
        }
    }

    private HttpHeaders createAuthHeaders(String token, String traceId, String sessionId) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.add(TRACE_ID_HEADER, traceId);
        headers.add(SESSION_ID_HEADER, sessionId);
        return headers;
    }

    private HttpHeaders createCorrelationHeaders(String traceId, String sessionId) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.add(TRACE_ID_HEADER, traceId);
        headers.add(SESSION_ID_HEADER, sessionId);
        return headers;
    }

    private String generateTraceId() {
        return UUID.randomUUID().toString().replace("-", "");
    }

    private record FilmSummary(Long id, String title, Integer release_year, String genre) {
    }
}

