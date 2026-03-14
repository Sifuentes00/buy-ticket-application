package com.matvey.cinema.service.impl;

import com.matvey.cinema.cache.InMemoryCache;
import com.matvey.cinema.exception.CustomNotFoundException;
import com.matvey.cinema.model.entities.Showtime;
import com.matvey.cinema.repository.ShowtimeRepository;
import com.matvey.cinema.service.ShowtimeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class ShowtimeServiceImpl implements ShowtimeService {

    private static final Logger logger = LoggerFactory.getLogger(ShowtimeServiceImpl.class);

    private final ShowtimeRepository showtimeRepository;
    private final InMemoryCache cache;

    public ShowtimeServiceImpl(ShowtimeRepository showtimeRepository, InMemoryCache cache) {
        this.showtimeRepository = showtimeRepository;
        this.cache = cache;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Showtime> findById(Long id) {
        String cacheKey = "showtime::id:" + id;
        logger.info("showtime.findById start id={}", id);

        Optional<Object> cached = cache.get(cacheKey);
        if (cached.isPresent()) {
            Object data = cached.get();
            if (data instanceof Showtime showtime) {
                logger.debug("showtime.cache.hit key={}", cacheKey);
                loadRelatedEntities(showtime);
                return Optional.of(showtime);
            }
            logger.warn("showtime.cache.invalidType key={} type={}", cacheKey, data.getClass().getName());
            cache.evict(cacheKey);
        }

        Optional<Showtime> showtime = showtimeRepository.findById(id);
        if (showtime.isEmpty()) {
            logger.warn("showtime.notFound id={}", id);
            throw new CustomNotFoundException("Сеанс не найден с ID: " + id);
        }

        showtime.ifPresent(s -> {
            loadRelatedEntities(s);
            cache.put(cacheKey, s);
            logger.debug("showtime.cache.store key={}", cacheKey);
        });

        logger.info("showtime.findById success id={}", id);
        return showtime;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Showtime> findAll() {
        String cacheKey = "showtime::all";
        logger.info("showtime.findAll start");

        Optional<Object> cached = cache.get(cacheKey);
        if (cached.isPresent()) {
            Object data = cached.get();
            if (data instanceof List<?> list) {
                if (list.isEmpty() || list.get(0) instanceof Showtime) {
                    try {
                        List<Showtime> showtimes = (List<Showtime>) data;
                        showtimes.forEach(this::loadRelatedEntities);
                        logger.debug("showtime.cache.hit key={}", cacheKey);
                        return showtimes;
                    } catch (ClassCastException e) {
                        logger.error("showtime.cache.castError key={}", cacheKey, e);
                    }
                }
            }
            cache.evict(cacheKey);
            logger.warn("showtime.cache.invalid key={}", cacheKey);
        }

        List<Showtime> showtimes = showtimeRepository.findAll();
        showtimes.forEach(this::loadRelatedEntities);
        cache.put(cacheKey, showtimes);

        logger.info("showtime.findAll success count={}", showtimes.size());
        return showtimes;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Showtime> findShowtimesByTheaterName(String theaterName) {
        String cacheKey = "showtime::by_theater_name:" + theaterName;
        logger.info("showtime.findByTheater start theater={}", theaterName);

        Optional<Object> cached = cache.get(cacheKey);
        if (cached.isPresent()) {
            Object data = cached.get();
            if (data instanceof List<?> list) {
                if (list.isEmpty() || list.get(0) instanceof Showtime) {
                    List<Showtime> showtimes = (List<Showtime>) data;
                    showtimes.forEach(this::loadRelatedEntities);
                    logger.debug("showtime.cache.hit key={}", cacheKey);
                    return showtimes;
                }
            }
            cache.evict(cacheKey);
        }

        List<Showtime> showtimes = showtimeRepository.findShowtimesByTheaterName(theaterName);
        if (showtimes == null || showtimes.isEmpty()) {
            logger.warn("showtime.notFound.theater theater={}", theaterName);
            return List.of();
        }

        showtimes.forEach(this::loadRelatedEntities);
        cache.put(cacheKey, showtimes);

        logger.info("showtime.findByTheater success theater={} count={}", theaterName, showtimes.size());
        return showtimes;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Showtime> findShowtimesByMovieTitle(String movieTitle) {
        String cacheKey = "showtime::by_movie_title:" + movieTitle;
        logger.info("showtime.findByMovieTitle start title={}", movieTitle);

        Optional<Object> cached = cache.get(cacheKey);
        if (cached.isPresent()) {
            Object data = cached.get();
            if (data instanceof List<?> list) {
                if (list.isEmpty() || list.get(0) instanceof Showtime) {
                    List<Showtime> showtimes = (List<Showtime>) data;
                    showtimes.forEach(this::loadRelatedEntities);
                    logger.debug("showtime.cache.hit key={}", cacheKey);
                    return showtimes;
                }
            }
            cache.evict(cacheKey);
        }

        List<Showtime> showtimes = showtimeRepository.findShowtimesByMovieTitle(movieTitle);
        if (showtimes == null || showtimes.isEmpty()) {
            logger.warn("showtime.notFound.movieTitle title={}", movieTitle);
            return List.of();
        }

        showtimes.forEach(this::loadRelatedEntities);
        cache.put(cacheKey, showtimes);

        logger.info("showtime.findByMovieTitle success title={} count={}", movieTitle, showtimes.size());
        return showtimes;
    }

    @Override
    @Transactional
    public Showtime save(Showtime showtime) {
        logger.info("showtime.save start id={}", showtime.getId());

        Showtime saved = showtimeRepository.save(showtime);

        cache.evict("showtime::all");
        if (saved.getId() != null) {
            cache.evict("showtime::id:" + saved.getId());
        }

        if (saved.getMovie() != null && saved.getMovie().getId() != null) {
            String key = "showtime::by_movie_id:" + saved.getMovie().getId();
            cache.evict(key);
            logger.debug("showtime.cache.evict key={}", key);
        }

        logger.info("showtime.save success id={}", saved.getId());
        return saved;
    }

    @Override
    @Transactional
    public void deleteById(Long id) {
        logger.info("showtime.delete start id={}", id);

        Optional<Showtime> showtimeOptional = showtimeRepository.findById(id);
        if (showtimeOptional.isEmpty()) {
            logger.warn("showtime.delete.notFound id={}", id);
            throw new CustomNotFoundException("Сеанс не найден с ID: " + id);
        }

        Showtime showtime = showtimeOptional.get();

        cache.evict("showtime::all");
        cache.evict("showtime::id:" + id);

        if (showtime.getMovie() != null && showtime.getMovie().getId() != null) {
            cache.evict("showtime::by_movie_id:" + showtime.getMovie().getId());
        }

        cache.evict("ticket::showtime:" + id);

        showtimeRepository.deleteById(id);

        logger.info("showtime.delete success id={}", id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Showtime> findShowtimesByMovieId(Long movieId) {
        String cacheKey = "showtime::by_movie_id:" + movieId;
        logger.info("showtime.findByMovieId start movieId={}", movieId);

        Optional<Object> cached = cache.get(cacheKey);
        if (cached.isPresent()) {
            Object data = cached.get();
            if (data instanceof List<?> list) {
                if (list.isEmpty() || list.get(0) instanceof Showtime) {
                    List<Showtime> showtimes = (List<Showtime>) data;
                    showtimes.forEach(this::loadRelatedEntities);
                    logger.debug("showtime.cache.hit key={}", cacheKey);
                    return showtimes;
                }
            }
            cache.evict(cacheKey);
        }

        List<Showtime> showtimes = showtimeRepository.findByMovieId(movieId);
        if (showtimes == null || showtimes.isEmpty()) {
            logger.warn("showtime.notFound.movieId movieId={}", movieId);
            return List.of();
        }

        showtimes.forEach(this::loadRelatedEntities);
        cache.put(cacheKey, showtimes);

        logger.info("showtime.findByMovieId success movieId={} count={}", movieId, showtimes.size());
        return showtimes;
    }

    private void loadRelatedEntities(Showtime showtime) {
        try {
            if (showtime.getMovie() != null) {
                showtime.getMovie().getTitle();
            }
            if (showtime.getTheater() != null) {
                showtime.getTheater().getName();
            }
        } catch (Exception e) {
            logger.error("showtime.loadRelations.error id={}", showtime.getId(), e);
        }
    }
}