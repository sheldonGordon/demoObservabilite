CREATE TABLE IF NOT EXISTS film (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    title VARCHAR(200) NOT NULL,
    original_title VARCHAR(200),
    release_year SMALLINT NOT NULL CHECK (release_year BETWEEN 1888 AND 2100),
    genre VARCHAR(80) NOT NULL,
    director VARCHAR(120) NOT NULL,
    duration_minutes SMALLINT NOT NULL CHECK (duration_minutes > 0),
    language VARCHAR(80) NOT NULL,
    country VARCHAR(80),
    age_rating VARCHAR(10),
    imdb_score NUMERIC(3,1) CHECK (imdb_score BETWEEN 0.0 AND 10.0),
    synopsis TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_film_title ON film (title);
CREATE INDEX IF NOT EXISTS idx_film_release_year ON film (release_year);

