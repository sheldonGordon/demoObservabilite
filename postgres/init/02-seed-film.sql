INSERT INTO film (
    title,
    original_title,
    release_year,
    genre,
    director,
    duration_minutes,
    language,
    country,
    age_rating,
    imdb_score,
    synopsis
)
SELECT
    format('Demo Film %s', s.id) AS title,
    format('Original Demo Film %s', s.id) AS original_title,
    1980 + (s.id % 45) AS release_year,
    (ARRAY['Action', 'Drama', 'Comedy', 'Thriller', 'Sci-Fi', 'Animation', 'Adventure', 'Crime', 'Fantasy', 'Horror'])[(s.id % 10) + 1] AS genre,
    format('Director %s', ((s.id - 1) % 20) + 1) AS director,
    85 + (s.id % 70) AS duration_minutes,
    (ARRAY['French', 'English', 'Spanish', 'Italian'])[(s.id % 4) + 1] AS language,
    (ARRAY['France', 'USA', 'Spain', 'Italy'])[(s.id % 4) + 1] AS country,
    (ARRAY['G', 'PG', 'PG-13', 'R'])[(s.id % 4) + 1] AS age_rating,
    ROUND((5.0 + ((s.id % 51) / 10.0))::numeric, 1) AS imdb_score,
    format('Synopsis de demonstration pour le film numero %s.', s.id) AS synopsis
FROM generate_series(1, 100) AS s(id)
WHERE NOT EXISTS (SELECT 1 FROM film);

