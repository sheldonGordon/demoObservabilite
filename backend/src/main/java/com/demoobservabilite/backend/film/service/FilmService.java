package com.demoobservabilite.backend.film.service;

import java.util.List;

import com.demoobservabilite.backend.film.dto.FilmDetailsResponse;
import com.demoobservabilite.backend.film.dto.FilmSummaryResponse;
import com.demoobservabilite.backend.film.model.Film;
import com.demoobservabilite.backend.film.repository.FilmRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class FilmService {

    private final FilmRepository filmRepository;

    public FilmService(FilmRepository filmRepository) {
        this.filmRepository = filmRepository;
    }

    @Transactional(readOnly = true)
    public List<FilmSummaryResponse> findAllFilms() {
        return filmRepository.findAll().stream()
                .map(film -> new FilmSummaryResponse(
                        film.getId(),
                        film.getTitle(),
                        film.getReleaseYear(),
                        film.getGenre()))
                .toList();
    }

    @Transactional(readOnly = true)
    public FilmDetailsResponse findFilmDetailsById(Long id) {
        Film film = filmRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Film not found"));

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

