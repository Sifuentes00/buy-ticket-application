package com.matvey.cinema.service.impl;

import com.matvey.cinema.cache.CacheKeys;
import com.matvey.cinema.cache.InMemoryCache;
import com.matvey.cinema.exception.CustomNotFoundException;
import com.matvey.cinema.model.entities.Showtime;
import com.matvey.cinema.repository.ShowtimeRepository;
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
class ShowtimeServiceImplTest {

    @Mock
    private ShowtimeRepository showtimeRepository;

    @Mock
    private InMemoryCache cache;

    @InjectMocks
    private ShowtimeServiceImpl showtimeService;

    private Showtime showtime;

    @BeforeEach
    void setUp() {
        showtime = new Showtime();
        showtime.setId(1L);
    }

    @Test
    void testFindById_ShowtimeFoundInCache() {
        String cacheKey = CacheKeys.SHOWTIME_PREFIX + showtime.getId();
        when(cache.get(cacheKey)).thenReturn(Optional.of(showtime));

        Optional<Showtime> foundShowtime = showtimeService.findById(showtime.getId());

        assertTrue(foundShowtime.isPresent());
        assertEquals(showtime, foundShowtime.get());
        verify(cache, times(1)).get(cacheKey);
        verify(showtimeRepository, never()).findById(anyLong());
    }

    @Test
    void testFindById_ShowtimeFoundInRepository() {
        when(cache.get(CacheKeys.SHOWTIME_PREFIX + showtime.getId())).thenReturn(Optional.empty());
        when(showtimeRepository.findById(showtime.getId())).thenReturn(Optional.of(showtime));

        Optional<Showtime> foundShowtime = showtimeService.findById(showtime.getId());

        assertTrue(foundShowtime.isPresent());
        assertEquals(showtime, foundShowtime.get());
        verify(showtimeRepository, times(1)).findById(showtime.getId());
        verify(cache, times(1)).put(CacheKeys.SHOWTIME_PREFIX + showtime.getId(), showtime);
    }

    @Test
    void testFindById_ShowtimeNotFound() {
        when(cache.get(CacheKeys.SHOWTIME_PREFIX + showtime.getId())).thenReturn(Optional.empty());
        when(showtimeRepository.findById(showtime.getId())).thenReturn(Optional.empty());

        Long showtimeId = showtime.getId(); // Получаем ID сеанса
        assertThrows(CustomNotFoundException.class, () -> showtimeService.findById(showtimeId));
        verify(showtimeRepository, times(1)).findById(showtime.getId());
    }

    @Test
    void testFindAll_ShowtimesFoundInCache() {
        String cacheKey = CacheKeys.SHOWTIMES_ALL;
        when(cache.get(cacheKey)).thenReturn(Optional.of(Collections.singletonList(showtime)));

        List<Showtime> showtimes = showtimeService.findAll();

        assertEquals(1, showtimes.size());
        assertEquals(showtime, showtimes.get(0));
        verify(cache, times(1)).get(cacheKey);
        verify(showtimeRepository, never()).findAll();
    }

    @Test
    void testFindAll_ShowtimesNotFoundInCache() {
        String cacheKey = CacheKeys.SHOWTIMES_ALL;
        when(cache.get(cacheKey)).thenReturn(Optional.empty());
        when(showtimeRepository.findAll()).thenReturn(Collections.singletonList(showtime));

        List<Showtime> showtimes = showtimeService.findAll();

        assertEquals(1, showtimes.size());
        assertEquals(showtime, showtimes.get(0));
        verify(showtimeRepository, times(1)).findAll();
        verify(cache, times(1)).put(cacheKey, Collections.singletonList(showtime));
    }

    @Test
    void testSave_ShowtimeSuccessfullySaved() {
        when(showtimeRepository.save(showtime)).thenReturn(showtime);

        Showtime savedShowtime = showtimeService.save(showtime);

        assertEquals(showtime, savedShowtime);
        verify(showtimeRepository, times(1)).save(showtime);
        verify(cache, times(1)).evict(CacheKeys.SHOWTIMES_ALL);
        verify(cache, times(1)).evict(CacheKeys.SHOWTIME_PREFIX + savedShowtime.getId());
    }

    @Test
    void testDeleteById_ShowtimeExists() {
        when(showtimeRepository.findById(showtime.getId())).thenReturn(Optional.of(showtime));

        showtimeService.deleteById(showtime.getId());

        verify(showtimeRepository, times(1)).deleteById(showtime.getId());
        verify(cache, times(1)).evict(CacheKeys.SHOWTIMES_ALL);
        verify(cache, times(1)).evict(CacheKeys.SHOWTIME_PREFIX + showtime.getId());
    }

    @Test
    void testDeleteById_ShowtimeNotFound() {
        when(showtimeRepository.findById(showtime.getId())).thenReturn(Optional.empty());

        Long showtimeId = showtime.getId(); // Получаем ID сеанса
        assertThrows(CustomNotFoundException.class, () -> showtimeService.deleteById(showtimeId));
        verify(showtimeRepository, never()).deleteById(showtime.getId());
    }

    @Test
    void testFindShowtimesByTheaterName_ShowtimesFoundInCache() {
        String theaterName = "Test Theater";
        String cacheKey = CacheKeys.SHOWTIMES_THEATER_PREFIX + theaterName;
        when(cache.get(cacheKey)).thenReturn(Optional.of(Collections.singletonList(showtime)));

        List<Showtime> showtimes = showtimeService.findShowtimesByTheaterName(theaterName);

        assertEquals(1, showtimes.size());
        assertEquals(showtime, showtimes.get(0));
        verify(cache, times(1)).get(cacheKey);
        verify(showtimeRepository, never()).findShowtimesByTheaterName(anyString());
    }

    @Test
    void testFindShowtimesByTheaterName_ShowtimesNotFoundInCache() {
        String theaterName = "Test Theater";
        String cacheKey = CacheKeys.SHOWTIMES_THEATER_PREFIX + theaterName;
        when(cache.get(cacheKey)).thenReturn(Optional.empty());
        when(showtimeRepository.findShowtimesByTheaterName(theaterName)).thenReturn(Collections.singletonList(showtime));

        List<Showtime> showtimes = showtimeService.findShowtimesByTheaterName(theaterName);

        assertEquals(1, showtimes.size());
        assertEquals(showtime, showtimes.get(0));
        verify(showtimeRepository, times(1)).findShowtimesByTheaterName(theaterName);
        verify(cache, times(1)).put(cacheKey, Collections.singletonList(showtime));
    }

    @Test
    void testFindShowtimesByMovieTitle_ShowtimesFoundInCache() {
        String movieTitle = "Test Movie";
        String cacheKey = CacheKeys.SHOWTIMES_MOVIE_PREFIX + movieTitle;
        when(cache.get(cacheKey)).thenReturn(Optional.of(Collections.singletonList(showtime)));

        List<Showtime> showtimes = showtimeService.findShowtimesByMovieTitle(movieTitle);

        assertEquals(1, showtimes.size());
        assertEquals(showtime, showtimes.get(0));
        verify(cache, times(1)).get(cacheKey);
        verify(showtimeRepository, never()).findShowtimesByMovieTitle(anyString());
    }

    @Test
    void testFindShowtimesByMovieTitle_ShowtimesNotFoundInCache() {
        String movieTitle = "Test Movie";
        String cacheKey = CacheKeys.SHOWTIMES_MOVIE_PREFIX + movieTitle;
        when(cache.get(cacheKey)).thenReturn(Optional.empty());
        when(showtimeRepository.findShowtimesByMovieTitle(movieTitle)).thenReturn(Collections.singletonList(showtime));

        List<Showtime> showtimes = showtimeService.findShowtimesByMovieTitle(movieTitle);

        assertEquals(1, showtimes.size());
        assertEquals(showtime, showtimes.get(0));
        verify(showtimeRepository, times(1)).findShowtimesByMovieTitle(movieTitle);
        verify(cache, times(1)).put(cacheKey, Collections.singletonList(showtime));
    }
}
