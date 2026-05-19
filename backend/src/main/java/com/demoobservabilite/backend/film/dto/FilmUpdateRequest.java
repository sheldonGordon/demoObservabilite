package com.demoobservabilite.backend.film.dto;

import java.math.BigDecimal;

import com.fasterxml.jackson.annotation.JsonProperty;

public record FilmUpdateRequest(
        String title,
        String originalTitle,
        @JsonProperty("release_year") Integer releaseYear,
        String genre,
        String director,
        Integer durationMinutes,
        String language,
        String country,
        String ageRating,
        BigDecimal imdbScore,
        String synopsis) {
}

