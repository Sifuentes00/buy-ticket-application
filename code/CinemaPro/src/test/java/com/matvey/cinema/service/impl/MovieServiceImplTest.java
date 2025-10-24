package com.matvey.cinema.service.impl;

import com.matvey.cinema.cache.CacheKeys;
import com.matvey.cinema.cache.InMemoryCache;
import com.matvey.cinema.exception.CustomNotFoundException;
import com.matvey.cinema.model.entities.Movie;
import com.matvey.cinema.repository.MovieRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MovieServiceImplTest {

    @Mock
    private MovieRepository movieRepository;

    @Mock
    private InMemoryCache cache;

    @InjectMocks
    private MovieServiceImpl movieService;

    private Movie movie;

    @BeforeEach
    void setUp() {
        movie = new Movie();
        movie.setId(1L);
        movie.setTitle("Test Movie");
        movie.setDirector("Test Director");
        movie.setReleaseYear(2023);
        movie.setGenre("Drama");
    }

    @Test
    void testFindById_MovieFoundInCache() {
        String cacheKey = CacheKeys.MOVIE_PREFIX + movie.getId();
        when(cache.get(cacheKey)).thenReturn(Optional.of(movie));

        Optional<Movie> foundMovie = movieService.findById(movie.getId());

        assertTrue(foundMovie.isPresent());
        assertEquals(movie, foundMovie.get());
        verify(cache, times(1)).get(cacheKey);
        verify(movieRepository, never()).findById(anyLong());
    }

    @Test
    void testFindById_MovieFoundInRepository() {
        when(cache.get(CacheKeys.MOVIE_PREFIX + movie.getId())).thenReturn(Optional.empty());
        when(movieRepository.findById(movie.getId())).thenReturn(Optional.of(movie));

        Optional<Movie> foundMovie = movieService.findById(movie.getId());

        assertTrue(foundMovie.isPresent());
        assertEquals(movie, foundMovie.get());
        verify(movieRepository, times(1)).findById(movie.getId());
        verify(cache, times(1)).put(CacheKeys.MOVIE_PREFIX + movie.getId(), movie);
    }

    @Test
    void testFindById_MovieNotFound() {
        when(cache.get(CacheKeys.MOVIE_PREFIX + movie.getId())).thenReturn(Optional.empty());
        when(movieRepository.findById(movie.getId())).thenReturn(Optional.empty());

        Long nonExistentMovieId = movie.getId(); // Предположим, что такого ID нет
        assertThrows(CustomNotFoundException.class, () -> movieService.findById(nonExistentMovieId));

        verify(movieRepository, times(1)).findById(movie.getId());
    }

    @Test
    void testFindAll_MoviesFoundInCache() {
        String cacheKey = CacheKeys.MOVIES_ALL;
        when(cache.get(cacheKey)).thenReturn(Optional.of(Collections.singletonList(movie)));

        List<Movie> movies = movieService.findAll();

        assertEquals(1, movies.size());
        assertEquals(movie, movies.get(0));
        verify(cache, times(1)).get(cacheKey);
        verify(movieRepository, never()).findAll();
    }

    @Test
    void testFindAll_MoviesNotFoundInCache() {
        String cacheKey = CacheKeys.MOVIES_ALL;
        when(cache.get(cacheKey)).thenReturn(Optional.empty());
        when(movieRepository.findAll()).thenReturn(Collections.singletonList(movie));

        List<Movie> movies = movieService.findAll();

        assertEquals(1, movies.size());
        assertEquals(movie, movies.get(0));
        verify(movieRepository, times(1)).findAll();
        verify(cache, times(1)).put(cacheKey, Collections.singletonList(movie));
    }

    @Test
    void testSave_MovieSuccessfullySaved() {
        when(movieRepository.save(movie)).thenReturn(movie);

        Movie savedMovie = movieService.save(movie);

        assertEquals(movie, savedMovie);
        verify(movieRepository, times(1)).save(movie);
        verify(cache, times(1)).evict(CacheKeys.MOVIES_ALL);
        verify(cache, times(1)).evict(CacheKeys.MOVIE_PREFIX + savedMovie.getId());
    }

    @Test
    void testDeleteById_MovieExists() {
        when(movieRepository.existsById(movie.getId())).thenReturn(true);

        movieService.deleteById(movie.getId());

        verify(movieRepository, times(1)).deleteById(movie.getId());
        verify(cache, times(1)).evict(CacheKeys.MOVIE_PREFIX + movie.getId());
    }

    @Test
    void testDeleteById_MovieNotFound() {
        when(movieRepository.existsById(movie.getId())).thenReturn(false);

        Long nonExistentMovieId = movie.getId(); // Предположим, что такого ID нет
        assertThrows(CustomNotFoundException.class, () -> movieService.deleteById(nonExistentMovieId));
        verify(movieRepository, never()).deleteById(movie.getId());
    }
}

