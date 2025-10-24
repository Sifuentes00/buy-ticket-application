package com.matvey.cinema.service.impl;

import com.matvey.cinema.cache.CacheKeys;
import com.matvey.cinema.cache.InMemoryCache;
import com.matvey.cinema.exception.CustomNotFoundException;
import com.matvey.cinema.model.entities.Seat;
import com.matvey.cinema.repository.SeatRepository;
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
class SeatServiceImplTest {

    @Mock
    private SeatRepository seatRepository;

    @Mock
    private InMemoryCache cache;

    @InjectMocks
    private SeatServiceImpl seatService;

    private Seat seat;

    @BeforeEach
    void setUp() {
        seat = new Seat(1, 1, true);
        seat.setId(1L);
    }

    @Test
    void testFindById_SeatFoundInCache() {
        String cacheKey = CacheKeys.SEAT_PREFIX + seat.getId();
        when(cache.get(cacheKey)).thenReturn(Optional.of(seat));

        Optional<Seat> foundSeat = seatService.findById(seat.getId());

        assertTrue(foundSeat.isPresent());
        assertEquals(seat, foundSeat.get());
        verify(cache, times(1)).get(cacheKey);
        verify(seatRepository, never()).findById(anyLong());
    }

    @Test
    void testFindById_SeatFoundInRepository() {
        when(cache.get(CacheKeys.SEAT_PREFIX + seat.getId())).thenReturn(Optional.empty());
        when(seatRepository.findById(seat.getId())).thenReturn(Optional.of(seat));

        Optional<Seat> foundSeat = seatService.findById(seat.getId());

        assertTrue(foundSeat.isPresent());
        assertEquals(seat, foundSeat.get());
        verify(seatRepository, times(1)).findById(seat.getId());
        verify(cache, times(1)).put(CacheKeys.SEAT_PREFIX + seat.getId(), seat);
    }

    @Test
    void testFindById_SeatNotFound() {
        when(cache.get(CacheKeys.SEAT_PREFIX + seat.getId())).thenReturn(Optional.empty());
        when(seatRepository.findById(seat.getId())).thenReturn(Optional.empty());

        Long seatId = seat.getId(); // Получаем ID места
        assertThrows(CustomNotFoundException.class, () -> seatService.findById(seatId));
        verify(seatRepository, times(1)).findById(seat.getId());
    }

    @Test
    void testFindAll_SeatsFoundInCache() {
        String cacheKey = CacheKeys.SEATS_ALL;
        when(cache.get(cacheKey)).thenReturn(Optional.of(Collections.singletonList(seat)));

        List<Seat> seats = seatService.findAll();

        assertEquals(1, seats.size());
        assertEquals(seat, seats.get(0));
        verify(cache, times(1)).get(cacheKey);
        verify(seatRepository, never()).findAll();
    }

    @Test
    void testFindAll_SeatsNotFoundInCache() {
        String cacheKey = CacheKeys.SEATS_ALL;
        when(cache.get(cacheKey)).thenReturn(Optional.empty());
        when(seatRepository.findAll()).thenReturn(Collections.singletonList(seat));

        List<Seat> seats = seatService.findAll();

        assertEquals(1, seats.size());
        assertEquals(seat, seats.get(0));
        verify(seatRepository, times(1)).findAll();
        verify(cache, times(1)).put(cacheKey, Collections.singletonList(seat));
    }

    @Test
    void testSave_SeatSuccessfullySaved() {
        when(seatRepository.save(seat)).thenReturn(seat);

        Seat savedSeat = seatService.save(seat);

        assertEquals(seat, savedSeat);
        verify(seatRepository, times(1)).save(seat);
        verify(cache, times(1)).evict(CacheKeys.SEATS_ALL);
        verify(cache, times(1)).evict(CacheKeys.SEAT_PREFIX + savedSeat.getId());
        verifyNoMoreInteractions(cache); // Проверка на отсутствие других взаимодействий
    }

    @Test
    void testDeleteById_SeatExists() {
        when(seatRepository.findById(seat.getId())).thenReturn(Optional.of(seat));

        seatService.deleteById(seat.getId());

        verify(seatRepository, times(1)).deleteById(seat.getId());
        verify(cache, times(1)).evict(CacheKeys.SEATS_ALL);
        verify(cache, times(1)).evict(CacheKeys.SEAT_PREFIX + seat.getId());
        verifyNoMoreInteractions(cache); // Проверка на отсутствие других взаимодействий
    }

    @Test
    void testDeleteById_SeatNotFound() {
        when(seatRepository.findById(seat.getId())).thenReturn(Optional.empty());

        Long seatId = seat.getId(); // Получаем ID места
        assertThrows(CustomNotFoundException.class, () -> seatService.deleteById(seatId));
        verify(seatRepository, never()).deleteById(seat.getId());
    }
}
