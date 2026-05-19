package com.demoobservabilite.backend.film.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record FilmSummaryResponse(
        Long id,
        String title,
        @JsonProperty("release_year") Integer releaseYear,
        String genre) {
}


