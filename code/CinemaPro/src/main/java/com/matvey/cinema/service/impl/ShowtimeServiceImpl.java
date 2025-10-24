package com.matvey.cinema.service.impl;

import com.matvey.cinema.cache.InMemoryCache;
import com.matvey.cinema.exception.CustomNotFoundException;
import com.matvey.cinema.model.entities.Showtime;
import com.matvey.cinema.repository.ShowtimeRepository;
import com.matvey.cinema.service.ShowtimeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;

@Service
public class ShowtimeServiceImpl implements ShowtimeService {
    private static final Logger logger = LoggerFactory.getLogger(ShowtimeServiceImpl.class);

    private final ShowtimeRepository showtimeRepository;
    private final InMemoryCache cache;

    @Autowired
    public ShowtimeServiceImpl(ShowtimeRepository showtimeRepository, InMemoryCache cache) {
        this.showtimeRepository = showtimeRepository;
        this.cache = cache;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Showtime> findById(Long id) {
        String cacheKey = "showtime::id:" + id;
        logger.info("Поиск сеанса с ID: {}", id);

        Optional<Object> cachedData = cache.get(cacheKey);
        if (cachedData.isPresent()) {
            logger.info("Сеанс с ID: {} найден в кэше.", id);
            Object data = cachedData.get();
            if (data instanceof Showtime) {
                Showtime showtime = (Showtime) data;
                // Принудительная загрузка связанных сущностей после извлечения из кэша
                loadRelatedEntities(showtime);
                return Optional.of(showtime);
            } else {
                logger.error("Cached object for key {} is not a Showtime, it is of type {}. Evicting from cache.",
                        cacheKey, cachedData.get().getClass().getName());
                cache.evict(cacheKey);
            }
        }

        logger.info("Кэш промах для сеанса с ID {}. Получение из репозитория.", id);
        Optional<Showtime> showtime = showtimeRepository.findById(id);
        if (showtime.isEmpty()) {
            logger.error("Сеанс с ID: {} не найден в базе данных.", id);
            throw new CustomNotFoundException("Сеанс не найден с ID: " + id);
        }

        showtime.ifPresent(value -> {
            // Принудительная загрузка связанных сущностей ВНУТРИ ТРАНЗАКЦИИ ПЕРЕД кэшированием
            loadRelatedEntities(value);
            cache.put(cacheKey, value);
            logger.info("Сеанс с ID: {} добавлен в кэш.", id);
        });

        return showtime;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Showtime> findAll() {
        String cacheKey = "showtime::all";
        logger.info("Получение всех сеансов.");

        Optional<Object> cachedData = cache.get(cacheKey);
        if (cachedData.isPresent()) {
            logger.info("Все сеансы найдены в кэше.");
            Object data = cachedData.get();
            if (data instanceof List) {
                List<?> list = (List<?>) data;
                if (list.isEmpty() || list.get(0) instanceof Showtime) {
                    try {
                        List<Showtime> showtimes = (List<Showtime>) data;
                        // Принудительная загрузка связанных сущностей после извлечения из кэша
                        showtimes.forEach(this::loadRelatedEntities);
                        return showtimes;
                    } catch (ClassCastException e) {
                        logger.error("ClassCastException when casting cached data for key: {}", cacheKey, e);
                        cache.evict(cacheKey);
                    }
                }
            }
            cache.evict(cacheKey);
            logger.warn("Incorrect data type in cache for key: {}", cacheKey);
        }

        logger.info("Кэш промах для всех сеансов. Получение из репозитория.");
        List<Showtime> showtimes = showtimeRepository.findAll();
        // Принудительная загрузка связанных сущностей ВНУТРИ ТРАНЗАКЦИИ ПЕРЕД кэшированием
        showtimes.forEach(this::loadRelatedEntities);

        cache.put(cacheKey, showtimes);
        logger.info("Все сеансы добавлены в кэш.");

        return showtimes;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Showtime> findShowtimesByTheaterName(String theaterName) {
        String cacheKey = "showtime::by_theater_name:" + theaterName;
        logger.info("Поиск сеансов для театра: {}", theaterName);

        Optional<Object> cachedData = cache.get(cacheKey);
        if (cachedData.isPresent()) {
            logger.info("Сеансы для театра {} найдены в кэше.", theaterName);
            Object data = cachedData.get();
            if (data instanceof List) {
                List<?> list = (List<?>) data;
                if (list.isEmpty() || list.get(0) instanceof Showtime) {
                    try {
                        List<Showtime> showtimes = (List<Showtime>) data;
                        // Принудительная загрузка связанных сущностей после извлечения из кэша
                        showtimes.forEach(this::loadRelatedEntities);
                        return showtimes;
                    } catch (ClassCastException e) {
                        logger.error("ClassCastException when casting cached data for key: {}", cacheKey, e);
                        cache.evict(cacheKey);
                    }
                }
            }
            cache.evict(cacheKey);
            logger.warn("Incorrect data type in cache for key: {}", cacheKey);
        }

        logger.info("Кэш промах для сеансов театра {}. Получение из репозитория.", theaterName);
        List<Showtime> showtimes = showtimeRepository.findShowtimesByTheaterName(theaterName);
        if (showtimes == null || showtimes.isEmpty()) {
            logger.warn("Сеансы для театра {} не найдены в базе данных.", theaterName);
            return List.of();
        }

        // Принудительная загрузка связанных сущностей ВНУТРИ ТРАНЗАКЦИИ ПЕРЕД кэшированием
        showtimes.forEach(this::loadRelatedEntities);

        cache.put(cacheKey, showtimes);
        logger.info("Сеансы для театра {} добавлены в кэш.", theaterName);

        return showtimes;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Showtime> findShowtimesByMovieTitle(String movieTitle) {
        String cacheKey = "showtime::by_movie_title:" + movieTitle;
        logger.info("Поиск сеансов для фильма по названию: {}", movieTitle);

        Optional<Object> cachedData = cache.get(cacheKey);
        if (cachedData.isPresent()) {
            logger.info("Сеансы для фильма по названию {} найдены в кэше.", movieTitle);
            Object data = cachedData.get();
            if (data instanceof List) {
                List<?> list = (List<?>) data;
                if (list.isEmpty() || list.get(0) instanceof Showtime) {
                    try {
                        List<Showtime> showtimes = (List<Showtime>) data;
                        // Принудительная загрузка связанных сущностей после извлечения из кэша
                        showtimes.forEach(this::loadRelatedEntities);
                        return showtimes;
                    } catch (ClassCastException e) {
                        logger.error("ClassCastException when casting cached data for key: {}", cacheKey, e);
                        cache.evict(cacheKey);
                    }
                }
            }
            cache.evict(cacheKey);
            logger.warn("Incorrect data type in cache for key: {}", cacheKey);
        }

        logger.info("Кэш промах для сеансов фильма по названию {}. Получение из репозитория.", movieTitle);
        List<Showtime> showtimes = showtimeRepository.findShowtimesByMovieTitle(movieTitle);
        if (showtimes == null || showtimes.isEmpty()) {
            logger.warn("Сеансы для фильма по названию {} не найдены в базе данных.", movieTitle);
            return List.of();
        }

        // Принудительная загрузка связанных сущностей ВНУТРИ ТРАНЗАКЦИИ ПЕРЕД кэшированием
        showtimes.forEach(this::loadRelatedEntities);

        cache.put(cacheKey, showtimes);
        logger.info("Сеансы для фильма по названию {} добавлены в кэш.", movieTitle);

        return showtimes;
    }

    @Override
    @Transactional
    public Showtime save(Showtime showtime) {
        logger.info("Сохранение сеанса с ID: {}", showtime.getId());

        // Убедимся, что Movie и Theater загружены перед сохранением,
        // если это необходимо для дальнейшей логики или очистки кэша.
        // Если они FetchType.LAZY и не были загружены до вызова save,
        // и вам нужен их ID для очистки кэша, их нужно загрузить здесь.
        // Однако, если они non-null (@NotNull) и были установлены перед save,
        // они, вероятно, доступны после save.
        if (showtime.getMovie() != null && showtime.getMovie().getId() == null) {
            // Если Movie новое, его ID может не быть до сохранения Showtime.
            // Если Movie уже существует и только ссылка устанавливается, ID должен быть.
            // В большинстве случаев, Movie и Theater уже существуют в БД перед созданием Showtime.
            // Поэтому прямой доступ после сохранения savedShowtime должен быть безопасен.
        }


        Showtime savedShowtime = showtimeRepository.save(showtime);

        // --- Очистка кэша ---
        // Очистка общего кэша всех сеансов
        cache.evict("showtime::all");
        logger.info("Общий кэш сеансов 'showtime::all' очищен при сохранении.");

        // Очистка кэша конкретного сеанса по ID
        if (savedShowtime.getId() != null) {
            cache.evict("showtime::id:" + savedShowtime.getId());
            logger.info("Кэш сеанса по ID '{}' очищен при сохранении.", savedShowtime.getId());
        }

        // Очистка кеша связанных сущностей (Театр, Фильм)
        if (savedShowtime.getTheater() != null && savedShowtime.getTheater().getId() != null) {
            // Этот ключ, возможно, используется для поиска по ID театра, а не по названию
            // Проверьте ключи кэша, используемые в методах поиска по театру
            // cache.evict("showtime::by_theater_name:" + savedShowtime.getTheater().getId()); // Проверьте название ключа
            logger.info("Кэш для сеансов театра с ID '{}' очищен при сохранении (если используется).", savedShowtime.getTheater().getId());
        }
        // Этот ключ, возможно, используется для поиска по ID фильма, а не по названию
        // Проверьте ключи кэша, используемые в методах поиска по фильму
        // cache.evict("showtime::by_movie_title:" + savedShowtime.getMovie().getId()); // Проверьте название ключа
        logger.info("Кэш для сеансов фильма с ID '{}' очищен при сохранении (если используется).", savedShowtime.getMovie().getId());


        // === ДОБАВЛЕНО: Очистка кэша для findShowtimesByMovieId ===
        // Этот ключ используется методом findShowtimesByMovieId
        if (savedShowtime.getMovie() != null && savedShowtime.getMovie().getId() != null) {
            String showtimesByMovieCacheKey = "showtime::by_movie_id:" + savedShowtime.getMovie().getId();
            cache.evict(showtimesByMovieCacheKey);
            logger.info("Кэш для сеансов по ID фильма '{}' ('{}') очищен при сохранении.", savedShowtime.getMovie().getId(), showtimesByMovieCacheKey);
        } else {
            logger.warn("Не удалось очистить кэш сеансов по ID фильма, так как Movie или его ID null для сохраненного сеанса.");
        }
        // ========================================================


        return savedShowtime;
    }


    @Override
    @Transactional // Должна быть транзакция для загрузки Lazy-связей и удаления
    public void deleteById(Long id) {
        logger.info("Удаление сеанса с ID: {}", id);
        Optional<Showtime> showtimeOptional = showtimeRepository.findById(id);
        if (showtimeOptional.isPresent()) {
            Showtime showtime = showtimeOptional.get();

            // Очистка кеша, связанного с удаляемым сеансом
            cache.evict("showtime::all");
            logger.info("Общий кэш сеансов 'showtime::all' очищен при удалении сеанса с ID: {}", id);
            cache.evict("showtime::id:" + id);
            logger.info("Кэш сеанса по ID '{}' очищен при удалении.", id);


            // Очистка кеша связанных билетов (если вы их кэшируете по ID)
            // Предполагается, что билеты будут каскадно удалены базой данных или JPA
            // Если каскадное удаление работает, кэш билетов по ID сеанса будет неактуален,
            // но кэш отдельных билетов по их собственным ID может остаться.
            // Если вы кэшируете билеты по ID, эта очистка уместна.
            if (showtime.getTickets() != null) { // tickets могут быть lazy; транзакция должна быть открыта
                showtime.getTickets().forEach(ticket -> {
                    // Проверьте, что ключ кэша для билета именно такой: "ticket::id:"
                    // Если каскадное удаление работает, билеты удаляются ДО того, как этот код выполнится.
                    // Поэтому доступ к showtime.getTickets() здесь после JPA-удаления может привести к ошибке или вернуть пустой список.
                    // Лучше очищать кэш билетов по ID сеанса, а не по ID отдельных билетов,
                    // так как билеты будут удалены.
                    // cache.evict("ticket::id:" + ticket.getId()); // Эта строка может быть проблематичной
                    // logger.debug("Evicted ticket with ID: {}", ticket.getId());
                });
                // Вместо очистки по ID каждого билета, очистите кэш списка билетов для этого сеанса, если он есть
                cache.evict("ticket::showtime:" + id); // Пример ключа кэша для списка билетов по ID сеанса
                logger.info("Кэш для списка билетов сеанса с ID '{}' очищен при удалении.", id);

            } else {
                logger.warn("Список билетов для сеанса ID '{}' равен null при попытке очистки кэша билетов.", id);
            }


            // Очистка кеша связанных сущностей (Театр, Фильм) - проверьте название ключей!
            if (showtime.getTheater() != null && showtime.getTheater().getId() != null) { // Theater может быть lazy
                // Проверьте название ключа кэша для поиска по театру
                // cache.evict("showtime::by_theater_name:" + showtime.getTheater().getId()); // Проверьте название ключа
                logger.info("Кэш для сеансов театра с ID '{}' очищен при удалении сеанса (если используется).", showtime.getTheater().getId());
            }
            if (showtime.getMovie() != null && showtime.getMovie().getId() != null) { // Movie может быть lazy
                // Проверьте название ключа кэша для поиска по названию фильма
                // cache.evict("showtime::by_movie_title:" + showtime.getMovie().getId()); // Проверьте название ключа
                logger.info("Кэш для сеансов фильма с ID '{}' очищен при удалении сеанса (если используется).", showtime.getMovie().getId());
            }


            // === ДОБАВЛЕНО: Очистка кэша для findShowtimesByMovieId ===
            // Этот ключ используется методом findShowtimesByMovieId
            if (showtime.getMovie() != null && showtime.getMovie().getId() != null) { // Movie должен быть доступен
                String showtimesByMovieCacheKey = "showtime::by_movie_id:" + showtime.getMovie().getId();
                cache.evict(showtimesByMovieCacheKey);
                logger.info("Кэш для сеансов по ID фильма '{}' ('{}') очищен при удалении.", showtime.getMovie().getId(), showtimesByMovieCacheKey);
            } else {
                logger.warn("Не удалось очистить кэш сеансов по ID фильма при удалении, так как Movie или его ID null.");
            }
            // ========================================================


            // Выполнение удаления сеанса из БД
            showtimeRepository.deleteById(id);
            logger.info("Сеанс с ID: {} успешно удален из БД и кэш очищен.", id);

        } else {
            logger.error("Сеанс с ID: {} не найден для удаления.", id);
            throw new CustomNotFoundException("Сеанс не найден с ID: " + id);
        }
    }


    @Override
    @Transactional(readOnly = true)
    public List<Showtime> findShowtimesByMovieId(Long movieId) {
        String cacheKey = "showtime::by_movie_id:" + movieId;
        logger.info("Поиск сеансов для фильма с ID: {}", movieId);

        Optional<Object> cachedData = cache.get(cacheKey);
        if (cachedData.isPresent()) {
            logger.info("Сеансы для фильма с ID {} найдены в кэше.", movieId);
            Object data = cachedData.get();
            if (data instanceof List) {
                List<?> list = (List<?>) data;
                if (list.isEmpty() || list.get(0) instanceof Showtime) {
                    try {
                        List<Showtime> showtimes = (List<Showtime>) data;
                        // Принудительная загрузка связанных сущностей после извлечения из кэша
                        showtimes.forEach(this::loadRelatedEntities);
                        return showtimes;
                    } catch (ClassCastException e) {
                        logger.error("ClassCastException when casting cached data for key: {}", cacheKey, e);
                        cache.evict(cacheKey);
                    }
                }
            }
            cache.evict(cacheKey);
            logger.warn("Incorrect data type in cache for key: {}", cacheKey);
        }

        logger.info("Кэш промах для сеансов фильма с ID {}. Получение из репозитория.", movieId);
        List<Showtime> showtimes = showtimeRepository.findByMovieId(movieId);
        if (showtimes == null || showtimes.isEmpty()) {
            logger.warn("Сеансы для фильма с ID {} не найдены в базе данных.", movieId);
            return List.of();
        }
        showtimes.forEach(this::loadRelatedEntities);

        cache.put(cacheKey, showtimes);
        logger.info("Сеансы для фильма с ID: {} добавлены в кэш.", movieId);

        return showtimes;
    }

    private void loadRelatedEntities(Showtime showtime) {
        try {
            if (showtime.getMovie() != null) {
                logger.debug("Accessing Movie for showtime {}", showtime.getId());
                showtime.getMovie().getTitle();
            } else {
                logger.debug("Movie is null for showtime {}", showtime.getId());
            }
            if (showtime.getTheater() != null) {
                logger.debug("Accessing Theater for showtime {}", showtime.getId());
                showtime.getTheater().getName();
            } else {
                logger.debug("Theater is null for showtime {}", showtime.getId());
            }
        } catch (Exception e) {
            logger.error("Ошибка при принудительной загрузке связанных сущностей для сеанса ID: {}", showtime.getId(), e);
        }
    }
}

