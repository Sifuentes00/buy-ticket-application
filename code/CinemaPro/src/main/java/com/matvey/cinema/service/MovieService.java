package com.matvey.cinema.service;

import com.matvey.cinema.model.dto.MovieRequest;
import com.matvey.cinema.model.entities.Movie;
import java.util.List;
import java.util.Optional;

public interface MovieService {
    Optional<Movie> findById(Long id);

    List<Movie> findAll();

    Movie save(Movie movie);

    void deleteById(Long id);

    List<Movie> findAllWithReviews();

    Movie createMovie(MovieRequest movieRequest);

    void evictMovieCache(Long movieId);
    void evictAllMoviesCache();
    void evictAllMoviesWithReviewsCache();

}
