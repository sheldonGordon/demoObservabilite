package com.demoobservabilite.backend.film.repository;

import com.demoobservabilite.backend.film.model.Film;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FilmRepository extends JpaRepository<Film, Long> {
}

