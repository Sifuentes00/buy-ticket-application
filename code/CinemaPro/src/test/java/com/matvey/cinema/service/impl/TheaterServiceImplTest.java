package com.matvey.cinema.service.impl;

import com.matvey.cinema.cache.CacheKeys;
import com.matvey.cinema.cache.InMemoryCache;
import com.matvey.cinema.exception.CustomNotFoundException;
import com.matvey.cinema.model.entities.Theater;
import com.matvey.cinema.repository.TheaterRepository;
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
class TheaterServiceImplTest {

    @Mock
    private TheaterRepository theaterRepository;

    @Mock
    private InMemoryCache cache;

    @InjectMocks
    private TheaterServiceImpl theaterService;

    private Theater theater;

    @BeforeEach
    void setUp() {
        theater = new Theater();
        theater.setId(1L);
        theater.setName("Test Theater");
        theater.setCapacity(100);
    }

    @Test
    void testFindById_TheaterFoundInCache() {
        String cacheKey = CacheKeys.THEATER_PREFIX + theater.getId();
        when(cache.get(cacheKey)).thenReturn(Optional.of(theater));

        Optional<Theater> foundTheater = theaterService.findById(theater.getId());

        assertTrue(foundTheater.isPresent());
        assertEquals(theater, foundTheater.get());
        verify(cache, times(1)).get(cacheKey);
        verify(theaterRepository, never()).findById(anyLong());
    }

    @Test
    void testFindById_TheaterFoundInRepository() {
        when(cache.get(CacheKeys.THEATER_PREFIX + theater.getId())).thenReturn(Optional.empty());
        when(theaterRepository.findById(theater.getId())).thenReturn(Optional.of(theater));

        Optional<Theater> foundTheater = theaterService.findById(theater.getId());

        assertTrue(foundTheater.isPresent());
        assertEquals(theater, foundTheater.get());
        verify(theaterRepository, times(1)).findById(theater.getId());
        verify(cache, times(1)).put(CacheKeys.THEATER_PREFIX + theater.getId(), theater);
    }

    @Test
    void testFindById_TheaterNotFound() {
        when(cache.get(CacheKeys.THEATER_PREFIX + theater.getId())).thenReturn(Optional.empty());
        when(theaterRepository.findById(theater.getId())).thenReturn(Optional.empty());

        Long theaterId = theater.getId(); // Получаем ID театра
        assertThrows(CustomNotFoundException.class, () -> theaterService.findById(theaterId));
        verify(theaterRepository, times(1)).findById(theater.getId());
    }

    @Test
    void testFindAll_TheatersFoundInCache() {
        String cacheKey = CacheKeys.THEATERS_ALL;
        when(cache.get(cacheKey)).thenReturn(Optional.of(Collections.singletonList(theater)));

        List<Theater> theaters = theaterService.findAll();

        assertEquals(1, theaters.size());
        assertEquals(theater, theaters.get(0));
        verify(cache, times(1)).get(cacheKey);
        verify(theaterRepository, never()).findAll();
    }

    @Test
    void testFindAll_TheatersNotFoundInCache() {
        String cacheKey = CacheKeys.THEATERS_ALL;
        when(cache.get(cacheKey)).thenReturn(Optional.empty());
        when(theaterRepository.findAll()).thenReturn(Collections.singletonList(theater));

        List<Theater> theaters = theaterService.findAll();

        assertEquals(1, theaters.size());
        assertEquals(theater, theaters.get(0));
        verify(theaterRepository, times(1)).findAll();
        verify(cache, times(1)).put(cacheKey, Collections.singletonList(theater));
    }

    @Test
    void testSave_TheaterSuccessfullySaved() {
        when(theaterRepository.save(theater)).thenReturn(theater);

        Theater savedTheater = theaterService.save(theater);

        assertEquals(theater, savedTheater);
        verify(theaterRepository, times(1)).save(theater);
        verify(cache, times(1)).evict(CacheKeys.THEATERS_ALL);
        verify(cache, times(1)).evict(CacheKeys.THEATER_PREFIX + savedTheater.getId());
    }

    @Test
    void testDeleteById_TheaterExists() {
        when(theaterRepository.findById(theater.getId())).thenReturn(Optional.of(theater));

        theaterService.deleteById(theater.getId());

        verify(theaterRepository, times(1)).deleteById(theater.getId());
        verify(cache, times(1)).evict(CacheKeys.THEATERS_ALL);
        verify(cache, times(1)).evict(CacheKeys.THEATER_PREFIX + theater.getId());
    }

    @Test
    void testDeleteById_TheaterNotFound() {
        when(theaterRepository.findById(theater.getId())).thenReturn(Optional.empty());

        Long theaterId = theater.getId(); // Получаем ID театра
        assertThrows(CustomNotFoundException.class, () -> theaterService.deleteById(theaterId));
        verify(theaterRepository, never()).deleteById(theater.getId());
    }
}
