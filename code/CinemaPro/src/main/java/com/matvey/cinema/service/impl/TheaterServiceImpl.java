package com.matvey.cinema.service.impl;

import com.matvey.cinema.cache.CacheKeys;
import com.matvey.cinema.cache.InMemoryCache;
import com.matvey.cinema.exception.CustomNotFoundException;
import com.matvey.cinema.model.entities.Theater;
import com.matvey.cinema.repository.TheaterRepository;
import com.matvey.cinema.service.TheaterService;
import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class TheaterServiceImpl implements TheaterService {
    private static final Logger logger = LoggerFactory.getLogger(TheaterServiceImpl.class);

    private final TheaterRepository theaterRepository;
    private final InMemoryCache cache;

    @Autowired
    public TheaterServiceImpl(TheaterRepository theaterRepository, InMemoryCache cache) {
        this.theaterRepository = theaterRepository;
        this.cache = cache;
    }

    @Override
    public Optional<Theater> findById(Long id) {
        String cacheKey = CacheKeys.THEATER_PREFIX + id;
        logger.info("Поиск театра с ID: {}", id);

        Optional<Object> cachedData = cache.get(cacheKey);
        if (cachedData.isPresent()) {
            logger.info("Театр с ID: {} найден в кэше.", id);
            return Optional.of((Theater) cachedData.get());
        }

        Optional<Theater> theater = theaterRepository.findById(id);
        if (theater.isEmpty()) {
            logger.error("Театр с ID: {} не найден.", id);
            throw new CustomNotFoundException("Театр не найден с ID: " + id);
        }

        theater.ifPresent(value -> {
            cache.put(cacheKey, value);
            logger.info("Театр с ID: {} добавлен в кэш.", id);
        });

        return theater;
    }

    @Override
    public List<Theater> findAll() {
        String cacheKey = CacheKeys.THEATERS_ALL;
        logger.info("Получение всех театров.");

        Optional<Object> cachedData = cache.get(cacheKey);
        if (cachedData.isPresent()) {
            logger.info("Все театры найдены в кэше.");
            return (List<Theater>) cachedData.get();
        }

        List<Theater> theaters = theaterRepository.findAll();
        cache.put(cacheKey, theaters);
        logger.info("Все театры добавлены в кэш.");

        return theaters;
    }

    @Override
    public Theater save(Theater theater) {
        Theater savedTheater = theaterRepository.save(theater);

        cache.evict(CacheKeys.THEATERS_ALL);
        cache.evict(CacheKeys.THEATER_PREFIX + savedTheater.getId());
        logger.info("Театр с ID: {} успешно сохранен и кэш очищен.", savedTheater.getId());

        return savedTheater;
    }

    @Override
    public void deleteById(Long id) {
        logger.info("Удаление театра с ID: {}", id);
        Optional<Theater> theaterOpt = theaterRepository.findById(id);
        if (theaterOpt.isEmpty()) {
            logger.error("Театр с ID: {} не найден для удаления.", id);
            throw new CustomNotFoundException("Театр не найден с ID: " + id);
        }

        cache.evict(CacheKeys.THEATERS_ALL);
        cache.evict(CacheKeys.THEATER_PREFIX + id);

        theaterRepository.deleteById(id);
        logger.info("Театр с ID: {} успешно удален и кэш очищен.", id);
    }
}
