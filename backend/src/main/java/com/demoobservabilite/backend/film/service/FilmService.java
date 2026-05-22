package com.demoobservabilite.backend.film.service;

import java.time.OffsetDateTime;
import java.util.List;

import com.demoobservabilite.backend.film.dto.FilmDetailsResponse;
import com.demoobservabilite.backend.film.dto.FilmSummaryResponse;
import com.demoobservabilite.backend.film.dto.FilmUpdateRequest;
import com.demoobservabilite.backend.film.model.Film;
import com.demoobservabilite.backend.film.repository.FilmRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class FilmService {

    private static final Logger LOGGER = LoggerFactory.getLogger(FilmService.class);

    private final FilmRepository filmRepository;

    public FilmService(FilmRepository filmRepository) {
        this.filmRepository = filmRepository;
    }

    @Transactional(readOnly = true)
    public List<FilmSummaryResponse> findAllFilms() {
        List<FilmSummaryResponse> films = filmRepository.findAll().stream()
                .map(film -> new FilmSummaryResponse(
                        film.getId(),
                        film.getTitle(),
                        film.getReleaseYear(),
                        film.getGenre()))
                .toList();
        LOGGER.info("Lecture BDD: {} films recuperes", films.size());
        return films;
    }

    @Transactional(readOnly = true)
    public FilmDetailsResponse findFilmDetailsById(Long id) {
        Film film = filmRepository.findById(id)
                .orElseThrow(() -> {
                    LOGGER.warn("Lecture BDD: film id={} introuvable", id);
                    return new ResponseStatusException(HttpStatus.NOT_FOUND, "Film not found");
                });

        LOGGER.info("Lecture BDD: detail film id={} recupere", id);

        return toDetailsResponse(film);
    }

    @Transactional
    public FilmDetailsResponse updateFilm(Long id, FilmUpdateRequest request) {
        Film film = filmRepository.findById(id)
                .orElseThrow(() -> {
                    LOGGER.warn("Ecriture BDD: film id={} introuvable pour mise a jour", id);
                    return new ResponseStatusException(HttpStatus.NOT_FOUND, "Film not found");
                });

        if (request.title() != null) {
            film.setTitle(request.title());
        }
        if (request.originalTitle() != null) {
            film.setOriginalTitle(request.originalTitle());
        }
        if (request.releaseYear() != null) {
            film.setReleaseYear(request.releaseYear());
        }
        if (request.genre() != null) {
            film.setGenre(request.genre());
        }
        if (request.director() != null) {
            film.setDirector(request.director());
        }
        if (request.durationMinutes() != null) {
            film.setDurationMinutes(request.durationMinutes());
        }
        if (request.language() != null) {
            film.setLanguage(request.language());
        }
        if (request.country() != null) {
            film.setCountry(request.country());
        }
        if (request.ageRating() != null) {
            film.setAgeRating(request.ageRating());
        }
        if (request.imdbScore() != null) {
            film.setImdbScore(request.imdbScore());
        }
        if (request.synopsis() != null) {
            film.setSynopsis(request.synopsis());
        }

        film.setUpdatedAt(OffsetDateTime.now());
        Film updatedFilm = filmRepository.save(film);
        LOGGER.info("Ecriture BDD: film id={} mis a jour", id);
        return toDetailsResponse(updatedFilm);
    }

    private FilmDetailsResponse toDetailsResponse(Film film) {
        return new FilmDetailsResponse(
                film.getId(),
                film.getTitle(),
                film.getOriginalTitle(),
                film.getReleaseYear(),
                film.getGenre(),
                film.getDirector(),
                film.getDurationMinutes(),
                film.getLanguage(),
                film.getCountry(),
                film.getAgeRating(),
                film.getImdbScore(),
                film.getSynopsis(),
                film.getCreatedAt(),
                film.getUpdatedAt());
    }
}

