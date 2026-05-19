package com.demoobservabilite.backend.film.dto;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

import com.fasterxml.jackson.annotation.JsonProperty;

public record FilmDetailsResponse(
        Long id,
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
        String synopsis,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt) {
}

