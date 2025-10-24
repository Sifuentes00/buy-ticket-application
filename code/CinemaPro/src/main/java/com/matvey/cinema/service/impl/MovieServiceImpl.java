package com.matvey.cinema.service.impl;

import com.matvey.cinema.cache.InMemoryCache;
import com.matvey.cinema.model.entities.Movie;
import com.matvey.cinema.repository.MovieRepository;
import com.matvey.cinema.service.MovieService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.matvey.cinema.exception.CustomNotFoundException;
import com.matvey.cinema.model.dto.MovieRequest;

import java.util.List;
import java.util.Optional;

@Service
public class MovieServiceImpl implements MovieService {

    private static final Logger logger = LoggerFactory.getLogger(MovieServiceImpl.class);

    private final MovieRepository movieRepository;
    private final InMemoryCache cache;

    @Autowired
    public MovieServiceImpl(MovieRepository movieRepository, InMemoryCache cache) {
        this.movieRepository = movieRepository;
        this.cache = cache;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Movie> findById(Long id) {
        String cacheKey = "movie::id:" + id;
        logger.info("Finding movie by ID: {}", id);

        Optional<Object> cachedData = cache.get(cacheKey);
        if (cachedData.isPresent()) {
            logger.info("Movie ID: {} found in cache.", id);
            Object data = cachedData.get();
            if (data instanceof Movie) {
                return Optional.of((Movie) data);
            } else {
                cache.evict(cacheKey);
                logger.warn("Incorrect data type in cache for key: {}", cacheKey);
            }
        }

        Optional<Movie> movie = movieRepository.findById(id);
        if (movie.isEmpty()) {
            logger.warn("Movie with ID: {} not found.", id);
            return Optional.empty();
        }

        movie.ifPresent(value -> {
            cache.put(cacheKey, value);
            logger.info("Movie with ID: {} added to cache.", id);
        });
        return movie;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Movie> findAll() {
        String cacheKey = "movie::all";
        logger.info("Finding all movies.");

        Optional<Object> cachedData = cache.get(cacheKey);
        if (cachedData.isPresent()) {
            logger.info("All movies found in cache.");
            Object data = cachedData.get();
            if (data instanceof List) {
                List<?> list = (List<?>) data;
                if (!list.isEmpty() && list.get(0) instanceof Movie) {
                    return (List<Movie>) data;
                } else if (list.isEmpty()) {
                    return (List<Movie>) data;
                }
            }
            cache.evict(cacheKey);
            logger.warn("Incorrect data type in cache for key: {}", cacheKey);
        }

        List<Movie> movies = movieRepository.findAll();
        cache.put(cacheKey, movies);
        logger.info("All movies added to cache.");
        return movies;
    }

    @Override
    @Transactional
    public Movie save(Movie movie) {
        logger.info("Saving movie with ID: {}", movie.getId());
        Movie savedMovie = movieRepository.save(movie);
        logger.info("Movie successfully saved with ID: {}", savedMovie.getId());

        // Очистка кеша
        cache.evict("movie::all");
        if (savedMovie.getId() != null) {
            cache.evict("movie::id:" + savedMovie.getId());
        }
        cache.evict("movie::all_with_reviews");

        return savedMovie;
    }

    @Override
    @Transactional
    public void deleteById(Long id) {
        logger.info("Deleting movie with ID: {}", id);
        Optional<Movie> movieOptional = movieRepository.findById(id);
        if (movieOptional.isPresent()) {
            Movie movie = movieOptional.get();

            // Очистка кеша связанных данных.  Важно делать это ДО удаления из репозитория.
            evictRelatedCache(movie);

            movieRepository.deleteById(id); //  Удаляем фильм ПОСЛЕ очистки кеша.
            logger.info("Movie with ID: {} successfully deleted.", id);
        } else {
            logger.warn("Movie with ID: {} not found for deletion.", id);
            throw new CustomNotFoundException("Movie not found with ID: " + id); // Используем CustomNotFoundException
        }
    }

    private void evictRelatedCache(Movie movie) {
        // Очистка кеша связанных отзывов
        if (movie.getReviews() != null) {
            movie.getReviews().forEach(review -> {
                String reviewCacheKey = "review::id:" + review.getId();
                cache.evict(reviewCacheKey);
                logger.debug("Evicted review: {}", reviewCacheKey);
            });
        }
        // Очистка кеша связанных сеансов и их билетов
        if (movie.getShowtimes() != null) {
            movie.getShowtimes().forEach(showtime -> {
                String showtimeCacheKey = "showtime::id:" + showtime.getId();
                cache.evict(showtimeCacheKey);
                logger.debug("Evicted showtime: {}", showtimeCacheKey);
                if (showtime.getTickets() != null) {
                    showtime.getTickets().forEach(ticket -> {
                        String ticketCacheKey = "ticket::id:" + ticket.getId();
                        cache.evict(ticketCacheKey);
                        logger.debug("Evicted ticket: {}", ticketCacheKey);
                    });
                }
            });
        }

        // Очистка кеша самого фильма и списков фильмов
        cache.evict("movie::id:" + movie.getId());
        cache.evict("movie::all");
        cache.evict("movie::all_with_reviews");
        logger.debug("Evicted movie and movie lists from cache.");
    }


    @Override
    @Transactional(readOnly = true)
    public List<Movie> findAllWithReviews() {
        String cacheKey = "movie::all_with_reviews";
        logger.info("Finding all movies with reviews");

        Optional<Object> cachedData = cache.get(cacheKey);
        if (cachedData.isPresent()) {
            logger.info("All movies with reviews found in cache.");
            Object data = cachedData.get();
            if (data instanceof List) {
                List<?> list = (List<?>) data;
                if (!list.isEmpty() && list.get(0) instanceof Movie) {
                    return (List<Movie>) data;
                } else if (list.isEmpty()) {
                    return (List<Movie>) data;
                }
            }
            cache.evict(cacheKey);
            logger.warn("Incorrect data type in cache for key: {}", cacheKey);
        }
        List<Movie> movies = movieRepository.findAllWithReviews();
        cache.put(cacheKey, movies);
        logger.info("All movies with reviews added to cache.");
        return movies;
    }

    @Override
    @Transactional
    public Movie createMovie(MovieRequest movieRequest) {
        logger.info("Creating movie from MovieRequest: {}", movieRequest.getTitle());
        Movie newMovie = new Movie();
        newMovie.setTitle(movieRequest.getTitle());
        newMovie.setDirector(movieRequest.getDirector());
        newMovie.setReleaseYear(movieRequest.getReleaseYear());
        newMovie.setGenre(movieRequest.getGenre());
        Movie savedMovie = save(newMovie);
        logger.info("Movie with ID '{}' successfully created and saved.", savedMovie.getId());
        return savedMovie;
    }

    @Override
    public void evictMovieCache(Long movieId) {
        String cacheKey = "movie::id:" + movieId;
        cache.evict(cacheKey);
        logger.debug("Evicted movie cache for ID: {}", movieId);
    }

    @Override
    public void evictAllMoviesCache() {
        String cacheKey = "movie::all";
        cache.evict(cacheKey);
        logger.debug("Evicted all movies cache.");
    }

    @Override
    public void evictAllMoviesWithReviewsCache() {
        String cacheKey = "movie::all_with_reviews";
        cache.evict(cacheKey);
        logger.debug("Evicted all movies with reviews cache.");
    }
}

