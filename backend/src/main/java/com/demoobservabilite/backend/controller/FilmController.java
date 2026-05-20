package com.demoobservabilite.backend.controller;

import java.util.List;

import com.demoobservabilite.backend.film.dto.FilmDetailsResponse;
import com.demoobservabilite.backend.film.dto.FilmSummaryResponse;
import com.demoobservabilite.backend.film.dto.FilmUpdateRequest;
import com.demoobservabilite.backend.film.service.FilmService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@CrossOrigin(origins = "http://localhost:4200")
@RequestMapping("/api/films")
public class FilmController {

    private static final Logger LOGGER = LoggerFactory.getLogger(FilmController.class);

    private final FilmService filmService;

    public FilmController(FilmService filmService) {
        this.filmService = filmService;
    }

    @GetMapping
    public List<FilmSummaryResponse> findAllFilms() {
        LOGGER.info("Requete recue sur /api/films traceId={} spanId={} sessionId={}",
                mdcValue("trace_id"),
                mdcValue("span_id"),
                mdcValue("frontend_session_id"));
        return filmService.findAllFilms();
    }

    @GetMapping("/{id}")
    public FilmDetailsResponse findFilmDetailsById(@PathVariable Long id) {
        LOGGER.info("Requete recue sur /api/films/{} traceId={} spanId={} sessionId={}",
                id,
                mdcValue("trace_id"),
                mdcValue("span_id"),
                mdcValue("frontend_session_id"));
        return filmService.findFilmDetailsById(id);
    }

    @PutMapping("/{id}")
    public FilmDetailsResponse updateFilm(@PathVariable Long id, @RequestBody FilmUpdateRequest request) {
        LOGGER.info("Requete recue sur PUT /api/films/{} traceId={} spanId={} sessionId={}",
                id,
                mdcValue("trace_id"),
                mdcValue("span_id"),
                mdcValue("frontend_session_id"));
        return filmService.updateFilm(id, request);
    }

    private static String mdcValue(String key) {
        String value = MDC.get(key);
        return value == null || value.isBlank() ? "n/a" : value;
    }
}
