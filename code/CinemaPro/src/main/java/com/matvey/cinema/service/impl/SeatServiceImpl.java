package com.matvey.cinema.service.impl;

import com.matvey.cinema.cache.CacheKeys;
import com.matvey.cinema.cache.InMemoryCache;
import com.matvey.cinema.exception.CustomNotFoundException;
import com.matvey.cinema.model.entities.Seat;
import com.matvey.cinema.repository.SeatRepository;
import com.matvey.cinema.service.SeatService;
import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class SeatServiceImpl implements SeatService {
    private static final Logger logger = LoggerFactory.getLogger(SeatServiceImpl.class);

    private final SeatRepository seatRepository;
    private final InMemoryCache cache;

    @Autowired
    public SeatServiceImpl(SeatRepository seatRepository, InMemoryCache cache) {
        this.seatRepository = seatRepository;
        this.cache = cache;
    }

    @Override
    public Optional<Seat> findById(Long id) {
        String cacheKey = CacheKeys.SEAT_PREFIX + id;
        logger.info("Поиск места с ID: {}", id);

        Optional<Object> cachedData = cache.get(cacheKey);
        if (cachedData.isPresent()) {
            logger.info("Место с ID: {} найдено в кэше.", id);
            return Optional.of((Seat) cachedData.get());
        }

        Optional<Seat> seat = seatRepository.findById(id);
        if (seat.isEmpty()) {
            throw new CustomNotFoundException("Место не найдено с ID: " + id);
        }

        seat.ifPresent(value -> {
            cache.put(cacheKey, value);
            logger.info("Место с ID: {} добавлено в кэш.", id);
        });

        return seat;
    }

    @Override
    public List<Seat> findAll() {
        String cacheKey = CacheKeys.SEATS_ALL;
        logger.info("Получение всех мест.");

        Optional<Object> cachedData = cache.get(cacheKey);
        if (cachedData.isPresent()) {
            logger.info("Все места найдены в кэше.");
            return (List<Seat>) cachedData.get();
        }

        List<Seat> seats = seatRepository.findAll();
        cache.put(cacheKey, seats);
        logger.info("Все места добавлены в кэш.");

        return seats;
    }

    @Override
    public List<Seat> findSeatsByTheaterName(String theaterName) {
        String cacheKey = CacheKeys.SEATS_THEATER_PREFIX + theaterName;
        logger.info("Поиск мест для театра:");

        Optional<Object> cachedData = cache.get(cacheKey);
        if (cachedData.isPresent()) {
            logger.info("Места для театра найдены в кэше.");
            return (List<Seat>) cachedData.get();
        }

        List<Seat> seats = seatRepository.findSeatsByTheaterName(theaterName);
        cache.put(cacheKey, seats);
        logger.info("Места для театра добавлены в кэш.");

        return seats;
    }

    @Override
    public Seat save(Seat seat) {
        Seat savedSeat = seatRepository.save(seat);

        Optional<Long> theaterIdOpt = seatRepository.findTheaterIdById(savedSeat.getId());

        cache.evict(CacheKeys.SEATS_ALL);
        cache.evict(CacheKeys.SEAT_PREFIX + savedSeat.getId());

        theaterIdOpt.ifPresent(theaterId -> {
            cache.evict(CacheKeys.SEATS_THEATER_PREFIX + theaterId);
            logger.info("Кэш для мест театра с ID '{}' очищен.", theaterId);
        });

        logger.info("Место с ID: {} успешно сохранено и кэш очищен.", savedSeat.getId());
        return savedSeat;
    }

    @Override
    public void deleteById(Long id) {
        logger.info("Удаление места с ID: {}", id);
        Optional<Seat> seatOpt = seatRepository.findById(id);
        if (seatOpt.isEmpty()) {
            throw new CustomNotFoundException("Место не найдено с ID: " + id);
        }

        Seat seat = seatOpt.get();
        Optional<Long> theaterIdOpt = seatRepository.findTheaterIdById(seat.getId());

        cache.evict(CacheKeys.SEATS_ALL);
        cache.evict(CacheKeys.SEAT_PREFIX + seat.getId());

        theaterIdOpt.ifPresent(theaterId -> {
            cache.evict(CacheKeys.SEATS_THEATER_PREFIX + theaterId);
            logger.info("Кэш для мест театра с ID '{}' очищен при удалении места.", theaterId);
        });

        logger.info("Место с ID: {} успешно удалено и кэш очищен.", seat.getId());
        seatRepository.deleteById(id);
    }
}

