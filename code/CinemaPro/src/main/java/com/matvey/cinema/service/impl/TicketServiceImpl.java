package com.matvey.cinema.service.impl;

import com.matvey.cinema.cache.CacheKeys;
import com.matvey.cinema.cache.InMemoryCache;
import com.matvey.cinema.model.dto.PurchaseRequestDto;
import com.matvey.cinema.model.dto.TicketRequest;
import com.matvey.cinema.model.entities.Seat;
import com.matvey.cinema.model.entities.Showtime;
import com.matvey.cinema.model.entities.Ticket;
import com.matvey.cinema.model.entities.User;
import com.matvey.cinema.repository.SeatRepository;
import com.matvey.cinema.repository.ShowtimeRepository;
import com.matvey.cinema.repository.TicketRepository;
import com.matvey.cinema.repository.UserRepository;
import com.matvey.cinema.service.TicketService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class TicketServiceImpl implements TicketService {
    private static final Logger logger = LoggerFactory.getLogger(TicketServiceImpl.class);

    private final TicketRepository ticketRepository;
    private final ShowtimeRepository showtimeRepository;
    private final UserRepository userRepository;
    private final SeatRepository seatRepository;
    private final InMemoryCache cache;

    private static final Pattern SEAT_PATTERN = Pattern.compile("(\\d+)-(\\d+)");


    @Autowired
    public TicketServiceImpl(TicketRepository ticketRepository,
                             ShowtimeRepository showtimeRepository,
                             UserRepository userRepository,
                             SeatRepository seatRepository,
                             InMemoryCache cache) {
        this.ticketRepository = ticketRepository;
        this.showtimeRepository = showtimeRepository;
        this.userRepository = userRepository;
        this.seatRepository = seatRepository;
        this.cache = cache;
    }


    @Override
    @Transactional(readOnly = true) // Транзакция только для чтения
    public List<Ticket> findByUserId(Long userId) { // Используем имя findByUserId, которое вызывается в контроллере
        // УДАЛЕНО: Логика кэширования (проверка наличия в кэше, извлечение, обработка)
        // String cacheKey = CacheKeys.TICKETS_USER_PREFIX + userId;
        logger.info("Finding tickets for user ID: {}", userId);

        // УДАЛЕНО: Проверка кэша
        // Optional<Object> cachedData = cache.get(cacheKey);
        // if (cachedData.isPresent()) {
        //     logger.info("Tickets for user ID {} found in cache.", userId);
        //     Object data = cachedData.get();
        //     if (data instanceof List) {
        //         List<?> list = (List<?>) data;
        //         if (list.isEmpty() || list.get(0) instanceof Ticket) {
        //             try {
        //                 List<Ticket> tickets = (List<Ticket>) data;
        //                 tickets.forEach(ticket -> {
        //                     // Проверка на null перед обращением к геттеру
        //                     if (ticket.getShowtime() != null) {
        //                         ticket.getShowtime().getMovie(); // Загрузка Movie через Showtime
        //                     }
        //                     if (ticket.getSeat() != null) {
        //                         ticket.getSeat(); // Загрузка Seat
        //                     }
        //                 });
        //                 return tickets;
        //             } catch (ClassCastException e) {
        //                 logger.error("ClassCastException when casting cached data for key: {}", cacheKey, e);
        //                 cache.evict(cacheKey); // Очистить некорректные данные в кэше
        //             }
        //         }
        //     }
        //     // Если данные в кэше некорректны, очистить
        //     cache.evict(cacheKey);
        //     logger.warn("Incorrect data type in cache for key: {}", cacheKey);
        // }

        // === Получение данных напрямую из репозитория (ОСТАВЛЕНО) ===
        // Получение данных из репозитория (без EntityGraph, предполагаем Lazy Loading)
        List<Ticket> tickets = ticketRepository.findByUserId(userId);
        // ============================================================


        // --- Принудительная загрузка связанных сущностей ВНУТРИ ТРАНЗАКЦИИ (ОСТАВЛЕНО) ---
        // Обходим каждый билет и обращаемся к геттерам связанных объектов
        tickets.forEach(ticket -> {
            // Проверка на null перед обращением к геттеру
            if (ticket.getShowtime() != null) {
                ticket.getShowtime().getMovie(); // Обращение к Movie через Showtime
            }
            if (ticket.getSeat() != null) {
                ticket.getSeat(); // Обращение к Seat
            }
        });
        // --- Конец принудительной загрузки ---

        // УДАЛЕНО: Логика добавления в кэш
        // Помещение полностью загруженных данных в кэш
        // cache.put(cacheKey, tickets);
        // logger.info("Tickets for user ID {} added to cache.", userId);

        return tickets; // Возвращаем полученные из БД билеты
    }

    // --- КОНЕЦ МЕТОДА findByUserId ---


    // Ваши существующие методы (оставлены для полноты, убедитесь, что они нужны и корректны):

    @Override
    @Transactional(readOnly = true)
    public Optional<Ticket> findById(Long id) {
        String cacheKey = CacheKeys.TICKET_PREFIX + id;
        logger.info("Finding ticket by ID: {}", id);

        Optional<Object> cachedData = cache.get(cacheKey);
        if (cachedData.isPresent()) {
            logger.info("Ticket ID: {} found in cache.", id);
            Object data = cachedData.get();
            if (data instanceof Ticket) {
                Ticket ticket = (Ticket) data;
                // Принудительная загрузка связей после извлечения из кэша
                if (ticket.getShowtime() != null) {
                    ticket.getShowtime().getMovie();
                }
                if (ticket.getSeat() != null) {
                    ticket.getSeat();
                }
                return Optional.of(ticket);
            } else {
                cache.evict(cacheKey);
                logger.warn("Incorrect data type in cache for key: {}", cacheKey);
            }
        }

        Optional<Ticket> ticket = ticketRepository.findById(id);
        if (ticket.isEmpty()) {
            logger.warn("Ticket with ID: {} not found.", id);
            return Optional.empty();
        }

        ticket.ifPresent(value -> {
            // Принудительная загрузка связанных сущностей перед кэшированием
            if(value.getShowtime() != null) {
                value.getShowtime().getMovie(); // Загрузка Movie через Showtime
            }
            if(value.getSeat() != null) {
                value.getSeat(); // Загрузка Seat
            }
            cache.put(cacheKey, value);
            logger.info("Ticket with ID: {} added to cache.", id);
        });


        return ticket;
    }

    @Override
    @Transactional(readOnly = true) // Транзакция только для чтения
    public List<Ticket> findAll() {
        String cacheKey = CacheKeys.TICKETS_ALL;
        logger.info("Getting all tickets.");

        Optional<Object> cachedData = cache.get(cacheKey);
        if (cachedData.isPresent()) {
            logger.info("All tickets found in cache.");
            Object data = cachedData.get();
            if (data instanceof List) {
                List<?> list = (List<?>) data;
                if (list.isEmpty() || list.get(0) instanceof Ticket) {
                    try {
                        List<Ticket> tickets = (List<Ticket>) data;
                        // Принудительная загрузка связей после извлечения из кэша
                        tickets.forEach(ticket -> {
                            if(ticket.getShowtime() != null) {
                                ticket.getShowtime().getMovie();
                            }
                            if(ticket.getSeat() != null) {
                                ticket.getSeat();
                            }
                        });
                        return tickets;
                    } catch (ClassCastException e) {
                        logger.error("ClassCastException when casting cached data for key: {}", cacheKey, e);
                        cache.evict(cacheKey);
                    }
                }
            }
            cache.evict(cacheKey);
            logger.warn("Incorrect data type in cache for key: {}", cacheKey);
        }

        List<Ticket> tickets = ticketRepository.findAll();

        // Принудительная загрузка связанных сущностей для всех билетов ВНУТРИ ТРАНЗАКЦИИ перед кэшированием
        tickets.forEach(ticket -> {
            if(ticket.getShowtime() != null) {
                ticket.getShowtime().getMovie(); // Загрузка Movie через Showtime
            }
            if(ticket.getSeat() != null) {
                ticket.getSeat(); // Загрузка Seat
            }
        });


        cache.put(cacheKey, tickets);
        logger.info("All tickets added to cache.");

        return tickets;
    }


    @Override
    @Transactional(readOnly = true)
    public List<Ticket> findTicketsByUserUsername(String userUsername) {
        String cacheKey = CacheKeys.TICKETS_USER_PREFIX + userUsername;
        logger.info("Finding tickets for user: {}", userUsername);

        Optional<Object> cachedData = cache.get(cacheKey);
        if (cachedData.isPresent()) {
            logger.info("Tickets for user {} found in cache.", userUsername);
            Object data = cachedData.get();
            if (data instanceof List) {
                List<?> list = (List<?>) data;
                if (list.isEmpty() || list.get(0) instanceof Ticket) {
                    try {
                        List<Ticket> tickets = (List<Ticket>) data;
                        // Принудительная загрузка связей после извлечения из кэша
                        tickets.forEach(ticket -> {
                            if(ticket.getShowtime() != null) {
                                ticket.getShowtime().getMovie();
                            }
                            if(ticket.getSeat() != null) {
                                ticket.getSeat();
                            }
                        });
                        return tickets;
                    } catch (ClassCastException e) {
                        logger.error("ClassCastException when casting cached data for key: {}", cacheKey, e);
                        cache.evict(cacheKey);
                    }
                }
            }
            cache.evict(cacheKey);
            logger.warn("Incorrect data type in cache for key: {}", cacheKey);
        }

        // Получение данных из репозитория
        List<Ticket> tickets = ticketRepository.findByUser_Username(userUsername);

        // --- Принудительная загрузка связанных сущностей ВНУТРИ ТРАНЗАКЦИИ ---
        tickets.forEach(ticket -> {
            if(ticket.getShowtime() != null) {
                ticket.getShowtime().getMovie(); // Загрузка Movie через Showtime
            }
            if(ticket.getSeat() != null) {
                ticket.getSeat(); // Загрузка Seat
            }
        });
        // --- Конец принудительной загрузки ---


        cache.put(cacheKey, tickets);
        logger.info("Tickets for user {} added to cache.", userUsername);

        return tickets;
    }


    @Override
    @Transactional(readOnly = true)
    public List<Ticket> findTicketsByShowtimeDateTime(String showtimeDateTime) {
        String cacheKey = CacheKeys.TICKETS_SHOWTIME_PREFIX + showtimeDateTime;
        logger.info("Finding tickets for showtime datetime: {}", showtimeDateTime);

        Optional<Object> cachedData = cache.get(cacheKey);
        if (cachedData.isPresent()) {
            logger.info("Tickets for showtime datetime {} found in cache.", showtimeDateTime);
            Object data = cachedData.get();
            if (data instanceof List) {
                List<?> list = (List<?>) data;
                if (list.isEmpty() || list.get(0) instanceof Ticket) {
                    try {
                        List<Ticket> tickets = (List<Ticket>) data;
                        // Принудительная загрузка связей после извлечения из кэша
                        tickets.forEach(ticket -> {
                            if(ticket.getShowtime() != null) {
                                ticket.getShowtime().getMovie();
                            }
                            if(ticket.getSeat() != null) {
                                ticket.getSeat();
                            }
                        });
                        return tickets;
                    } catch (ClassCastException e) {
                        logger.error("ClassCastException when casting cached data for key: {}", cacheKey, e);
                        cache.evict(cacheKey);
                    }
                }
            }
            cache.evict(cacheKey);
            logger.warn("Incorrect data type in cache for key: {}", cacheKey);
        }

        // Получение данных из репозитория
        List<Ticket> tickets = ticketRepository.findByShowtime_DateTime(showtimeDateTime);

        // --- Принудительная загрузка связанных сущностей ВНУТРИ ТРАНЗАКЦИИ ---
        tickets.forEach(ticket -> {
            if(ticket.getShowtime() != null) {
                ticket.getShowtime().getMovie(); // Загрузка Movie через Showtime
            }
            if(ticket.getSeat() != null) {
                ticket.getSeat(); // Загрузка Seat
            }
        });
        // --- Конец принудительной загрузки ---


        cache.put(cacheKey, tickets);
        logger.info("Tickets for showtime datetime {} added to cache.", showtimeDateTime);

        return tickets;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Ticket> findTicketsBySeatId(Long seatId) {
        String cacheKey = CacheKeys.TICKETS_SEAT_PREFIX + seatId;
        logger.info("Finding tickets for seat ID: {}", seatId);

        Optional<Object> cachedData = cache.get(cacheKey);
        if (cachedData.isPresent()) {
            logger.info("Tickets for seat ID {} found in cache.", seatId);
            Object data = cachedData.get();
            if (data instanceof List) {
                List<?> list = (List<?>) data;
                if (list.isEmpty() || list.get(0) instanceof Ticket) {
                    try {
                        List<Ticket> tickets = (List<Ticket>) data;
                        // Принудительная загрузка связей после извлечения из кэша
                        tickets.forEach(ticket -> {
                            if(ticket.getShowtime() != null) {
                                ticket.getShowtime().getMovie();
                            }
                            if(ticket.getSeat() != null) {
                                ticket.getSeat();
                            }
                        });
                        return tickets;
                    } catch (ClassCastException e) {
                        logger.error("ClassCastException when casting cached data for key: {}", cacheKey, e);
                        cache.evict(cacheKey);
                    }
                }
            }
            cache.evict(cacheKey);
            logger.warn("Incorrect data type in cache for key: {}", cacheKey);
        }

        // Получение данных из репозитория
        List<Ticket> tickets = ticketRepository.findBySeatId(seatId);

        // --- Принудительная загрузка связанных сущностей ВНУТРИ ТРАНЗАКЦИИ ---
        tickets.forEach(ticket -> {
            if(ticket.getShowtime() != null) {
                ticket.getShowtime().getMovie(); // Загрузка Movie через Showtime
            }
            if(ticket.getSeat() != null) {
                ticket.getSeat(); // Загрузка Seat
            }
        });
        // --- Конец принудительной загрузки ---


        cache.put(cacheKey, tickets);
        logger.info("Tickets for seat ID {} added to cache.", seatId);

        return tickets;
    }


    @Transactional
    @Override
    public Ticket save(Ticket ticket) {
        logger.info("Saving ticket with ID: {}", ticket.getId());

        Ticket savedTicket = ticketRepository.save(ticket);
        logger.info("Ticket successfully saved with ID: {}", savedTicket.getId());

        // Очистка кэша после сохранения
        cache.evict(CacheKeys.TICKETS_ALL);
        if (savedTicket.getId() != null) {
            cache.evict(CacheKeys.TICKET_PREFIX + savedTicket.getId());
        }

        Optional.ofNullable(savedTicket.getUser()).map(User::getId).ifPresent(userId -> {
            cache.evict(CacheKeys.TICKETS_USER_PREFIX + userId);
            logger.info("Cache for tickets of user ID '{}' cleared upon saving.", userId);
        });
        Optional.ofNullable(savedTicket.getShowtime()).map(Showtime::getId).ifPresent(showtimeId -> {
            cache.evict(CacheKeys.TICKETS_SHOWTIME_PREFIX + showtimeId); // Очищаем также кэш для findByShowtimeId и findTicketsByShowtimeDateTime
            logger.info("Cache for tickets of showtime ID '{}' cleared upon saving.", showtimeId);
        });

        Optional.ofNullable(savedTicket.getSeat()).map(Seat::getId).ifPresent(seatId -> {
            cache.evict(CacheKeys.TICKETS_SEAT_PREFIX + seatId);
            logger.info("Cache for tickets of seat ID '{}' cleared upon saving.", seatId);
        });

        return savedTicket;
    }

    @Transactional
    @Override
    public void deleteById(Long id) {
        logger.info("Deleting ticket with ID: {}", id);
        Optional<Ticket> ticketOpt = ticketRepository.findById(id);
        if (ticketOpt.isEmpty()) {
            logger.warn("Ticket with ID: {} not found for deletion.", id);
            throw new RuntimeException("Ticket not found with ID: " + id);
        }

        Ticket ticket = ticketOpt.get();

        // Очистка кэша перед удалением
        cache.evict(CacheKeys.TICKETS_ALL);
        cache.evict(CacheKeys.TICKET_PREFIX + ticket.getId());

        Optional.ofNullable(ticket.getUser()).map(User::getId).ifPresent(userId -> {
            cache.evict(CacheKeys.TICKETS_USER_PREFIX + userId);
            logger.info("Cache for tickets of user ID '{}' cleared upon deletion.", userId);
        });
        Optional.ofNullable(ticket.getShowtime()).map(Showtime::getId).ifPresent(showtimeId -> {
            cache.evict(CacheKeys.TICKETS_SHOWTIME_PREFIX + showtimeId); // Очищаем кэш для findTicketsByShowtimeDateTime и findByShowtimeId
            logger.info("Cache for tickets of showtime ID '{}' cleared upon deletion.", showtimeId);
        });

        Optional.ofNullable(ticket.getSeat()).map(Seat::getId).ifPresent(seatId -> {
            cache.evict(CacheKeys.TICKETS_SEAT_PREFIX + seatId);
            logger.info("Cache for tickets of seat ID '{}' cleared upon deletion.", seatId);
        });


        ticketRepository.deleteById(id);
        logger.info("Ticket with ID: {} successfully deleted from DB.", id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Ticket> findByShowtimeId(Long showtimeId) {
        String cacheKey = CacheKeys.TICKETS_SHOWTIME_PREFIX + showtimeId;
        logger.info("Finding tickets for showtime ID: {}", showtimeId);

        Optional<Object> cachedData = cache.get(cacheKey);
        if (cachedData.isPresent()) {
            logger.info("Tickets for showtime ID {} found in cache.", showtimeId);
            Object data = cachedData.get();
            if (data instanceof List) {
                List<?> list = (List<?>) data;
                if (list.isEmpty() || list.get(0) instanceof Ticket) {
                    try {
                        List<Ticket> tickets = (List<Ticket>) data;
                        // Принудительная загрузка связей после извлечения из кэша
                        tickets.forEach(ticket -> {
                            if(ticket.getShowtime() != null) {
                                ticket.getShowtime().getMovie();
                            }
                            if(ticket.getSeat() != null) {
                                ticket.getSeat();
                            }
                        });
                        return tickets;
                    } catch (ClassCastException e) {
                        logger.error("ClassCastException when casting cached data for key: {}", cacheKey, e);
                        cache.evict(cacheKey);
                    }
                }
            }
            cache.evict(cacheKey);
            logger.warn("Incorrect data type in cache for key: {}", cacheKey);
        }

        // Получение данных из репозитория
        List<Ticket> tickets = ticketRepository.findByShowtime_Id(showtimeId);

        // --- Принудительная загрузка связанных сущностей ВНУТРИ ТРАНЗАКЦИИ ---
        tickets.forEach(ticket -> {
            if(ticket.getShowtime() != null) {
                ticket.getShowtime().getMovie(); // Загрузка Movie через Showtime
            }
            if(ticket.getSeat() != null) {
                ticket.getSeat(); // Загрузка Seat
            }
        });
        // --- Конец принудительной загрузки ---


        cache.put(cacheKey, tickets);
        logger.info("Tickets for showtime ID {} added to cache.", showtimeId);

        return tickets;
    }

    @Transactional // Транзакция для всей операции покупки
    @Override
    public List<Ticket> purchaseTickets(PurchaseRequestDto purchaseRequest) {
        logger.info("Starting ticket purchase process for showtime ID: {}, user ID: {}, seats: {}",
                purchaseRequest.getShowtimeId(), purchaseRequest.getUserId(), purchaseRequest.getSeatNumbers().size());

        // 1. Находим сеанс
        Showtime showtime = showtimeRepository.findById(purchaseRequest.getShowtimeId())
                .orElseThrow(() -> {
                    logger.error("Showtime not found with ID: {}", purchaseRequest.getShowtimeId());
                    return new RuntimeException("Showtime not found with ID: " + purchaseRequest.getShowtimeId());
                });
        logger.debug("Found showtime: {}", showtime.getId());

        // 2. Находим пользователя
        User user = userRepository.findById(purchaseRequest.getUserId())
                .orElseThrow(() -> {
                    logger.error("User not found with ID: {}", purchaseRequest.getUserId());
                    return new RuntimeException("User not found with ID: " + purchaseRequest.getUserId());
                });
        logger.debug("Found user: {}", user.getId());

        List<Ticket> createdTickets = new ArrayList<>();
        BigDecimal basePrice = BigDecimal.valueOf(300.0); // TODO: Determine real price based on showtime/seat/etc.


        // 3. Обрабатываем каждое выбранное место
        for (String seatNumber : purchaseRequest.getSeatNumbers()) {
            logger.debug("Checking and creating ticket for seat: {}", seatNumber);

            // Парсим номер места в формате "Ряд-Место"
            Matcher matcher = SEAT_PATTERN.matcher(seatNumber);
            if (!matcher.matches()) {
                logger.warn("Invalid seat number format received: {}", seatNumber);
                throw new IllegalArgumentException("Invalid seat number format: " + seatNumber);
            }
            int row = Integer.parseInt(matcher.group(1));
            int number = Integer.parseInt(matcher.group(2));

            // Находим Seat entity по ряду и номеру
            // Предполагается, что findBySeatRowAndNumber в SeatRepository существует
            Optional<Seat> seatOpt = seatRepository.findBySeatRowAndNumber(row, number);
            Seat seatEntity = seatOpt.orElseThrow(() -> {
                logger.error("Seat entity not found for row {} and number {}", row, number);
                return new RuntimeException("Seat entity not found for number: " + seatNumber);
            });


            // Проверяем, занято ли место для этого сеанса, используя seatNumber
            // Предполагается, что findByShowtimeAndSeatNumber в TicketRepository существует
            Optional<Ticket> existingTicket = ticketRepository.findByShowtimeAndSeatNumber(showtime, seatNumber);
            if (existingTicket.isPresent()) {
                logger.warn("Seat {} is already occupied for showtime ID: {}. Purchase attempt failed.", seatNumber, showtime.getId());
                // Бросаем исключение, чтобы транзакция откатилась
                throw new IllegalStateException("Seat " + seatNumber + " is already occupied.");
            }

            // Место свободно, создаем новый билет
            Ticket newTicket = new Ticket();
            newTicket.setShowtime(showtime); // Связываем с сеансом
            newTicket.setUser(user); // Связываем с пользователем
            newTicket.setSeat(seatEntity); // Связываем с сущностью Seat

            // !!! ИСПРАВЛЕНИЕ: Устанавливаем строковое представление номера места !!!
            newTicket.setSeatNumber(seatNumber); // <-- Устанавливаем значение из входящего запроса

            newTicket.setPrice(basePrice); // Устанавливаем цену
            // Если у вас есть поле purchaseTime, установите его здесь:
            // newTicket.setPurchaseTime(LocalDateTime.now());


            // Сохраняем новый билет в базу данных
            Ticket savedTicket = ticketRepository.save(newTicket);
            createdTickets.add(savedTicket);
            logger.debug("Created and saved ticket with ID: {} for seat {}", savedTicket.getId(), seatNumber);
        }

        logger.info("Purchase process completed successfully. Created tickets: {}. Clearing cache...", createdTickets.size());

        // 4. Очистка кэша после покупки
        // Очистка кэша для findByShowtimeId и findTicketsByShowtimeDateTime
        if (showtime.getId() != null) {
            cache.evict(CacheKeys.TICKETS_SHOWTIME_PREFIX + showtime.getId());
            // Если используется кэш по дате/времени, также очистить его
            // if (showtime.getDateTime() != null) {
            //      cache.evict(CacheKeys.TICKETS_SHOWTIME_PREFIX + showtime.getDateTime());
            // }
            logger.info("Cache for showtime ID '{}' cleared after purchase.", showtime.getId());
        }
        // Очистка кэша для findByUserId и findTicketsByUserUsername
        if (user.getId() != null) {
            cache.evict(CacheKeys.TICKETS_USER_PREFIX + user.getId()); // Ключ для findByUserId
            // Если используется кэш по username, также очистить его
            // if (user.getUsername() != null) {
            //     cache.evict(CacheKeys.TICKETS_USER_PREFIX + user.getUsername());
            // }
            logger.info("Cache for user ID '{}' cleared after purchase.", user.getId());
        }
        // Очистка кэша для конкретных билетов и связанных мест
        for (Ticket ticket : createdTickets) {
            if (ticket.getSeat() != null && ticket.getSeat().getId() != null) {
                // Очистка кэша для findTicketsBySeatId
                cache.evict(CacheKeys.TICKETS_SEAT_PREFIX + ticket.getSeat().getId());
                logger.info("Cache for tickets of seat ID '{}' cleared after purchase.", ticket.getSeat().getId());
            }
            // Очистка кэша для отдельного билета
            if (ticket.getId() != null) {
                cache.evict(CacheKeys.TICKET_PREFIX + ticket.getId());
                logger.info("Cache for ticket ID '{}' cleared after purchase.", ticket.getId());
            }
        }
        cache.evict(CacheKeys.TICKETS_ALL); // Очистка кэша для findAll

        return createdTickets; // Возвращаем список созданных билетов
    }


    @Override
    @Transactional(readOnly = true)
    public Optional<Ticket> findByShowtimeAndSeatNumber(Showtime showtime, String seatNumber) {
        logger.info("Finding ticket for showtime ID: {} and seat number: {}", showtime.getId(), seatNumber);

        if (cache != null) {
            String cacheKey = CacheKeys.TICKET_SHOWTIME_SEAT_PREFIX + showtime.getId() + "_" + seatNumber;
            // Получаем данные из кэша как Optional<Object>
            Optional<Object> cachedData = Optional.ofNullable(cache.get(cacheKey));

            if (cachedData.isPresent()) {
                Object cachedObject = cachedData.get();
                // Безопасная проверка типа и приведение
                if (cachedObject instanceof Ticket) {
                    Ticket ticket = (Ticket) cachedObject;
                    logger.info("Ticket for showtime ID {} and seat number {} found in cache and is of correct type.", showtime.getId(), seatNumber);

                    return Optional.of(ticket);
                } else {
                    logger.error("Cached object for key {} is not a Ticket, it is of type {}. Evicting from cache.",
                            cacheKey, cachedObject.getClass().getName());
                    // Если тип не совпадает, удаляем некорректный объект из кэша
                    cache.evict(cacheKey);
                    // И продолжаем поиск в репозитории/БД
                }
            }
        } else {
            logger.warn("Cache '{}' is not initialized. Skipping cache lookup.", CacheKeys.TICKET_BY_SHOWTIME_AND_SEAT);
        }


        // Если объект не найден в кэше или был некорректного типа, ищем в репозитории/БД
        logger.info("Searching for ticket for showtime ID: {} and seat number: {} in repository.", showtime.getId(), seatNumber);
        Optional<Ticket> ticket = ticketRepository.findByShowtimeAndSeatNumber(showtime, seatNumber);

        // Помещение в кэш только если билет найден и кэш инициализирован
        if (cache != null) {
            ticket.ifPresent(t -> {
                // Принудительная загрузка связей перед кэшированием (если необходимо)
                // Убедитесь, что связи загружены до того, как объект попадет в кэш,
                // чтобы избежать LazyInitializationException при извлечении.
                // if (t.getShowtime() != null) { t.getShowtime().getMovie(); }
                // if (t.getSeat() != null) { t.getSeat(); }

                // Убедитесь, что t.getSeatNumber() возвращает правильное значение для ключа
                String cacheKey = CacheKeys.TICKET_SHOWTIME_SEAT_PREFIX + t.getShowtime().getId() + "_" + t.getSeat().getNumber(); // Используем getSeat().getSeatNumber() для надежности
                cache.put(cacheKey, t);
                logger.info("Ticket for showtime ID {} and seat number {} added to cache.", t.getShowtime().getId(), t.getSeat().getNumber());
            });
        }


        return ticket;
    }


    @Transactional
    @Override
    public Ticket mapTicketRequestToTicket(TicketRequest ticketRequest) {
        logger.debug("Mapping TicketRequest DTO to Ticket entity");
        Ticket ticket = new Ticket();
        // Handle null price from DTO (now it's Double)
        if (ticketRequest.getPrice() != null) {
            ticket.setPrice(BigDecimal.valueOf(ticketRequest.getPrice()));
        } else {
            ticket.setPrice(null); // Или установите значение по умолчанию, или выбросьте исключение
            logger.warn("Price is null in TicketRequest for mapping.");
        }


        if (ticketRequest.getShowtimeId() != null) {
            Showtime showtime = showtimeRepository.findById(ticketRequest.getShowtimeId())
                    .orElseThrow(() -> new RuntimeException("Showtime not found with ID: " + ticketRequest.getShowtimeId()));
            ticket.setShowtime(showtime);
        } else {
            throw new RuntimeException("Showtime ID in request cannot be null.");
        }

        if (ticketRequest.getUserId() != null) {
            User user = userRepository.findById(ticketRequest.getUserId())
                    .orElseThrow(() -> new RuntimeException("User not found with ID: " + ticketRequest.getUserId()));
            ticket.setUser(user);
        } else {
            throw new RuntimeException("User ID in request cannot be null.");
        }

        // Find Seat by ID from DTO and set relation.
        if (ticketRequest.getSeatId() != null) {
            Seat seat = seatRepository.findById(ticketRequest.getSeatId())
                    .orElseThrow(() -> new RuntimeException("Seat not found with ID: " + ticketRequest.getSeatId()));
            ticket.setSeat(seat);
            // Derive seatNumber string from Seat entity data IF Ticket entity still stores it as a separate field
            // If Ticket only uses the Seat relation, you don't need this line or the seatNumber field in Ticket
            ticket.setSeatNumber(seat.getSeatRow() + "-" + seat.getNumber()); // Assuming Seat has getSeatRow() and getNumber()
        } else {
            throw new RuntimeException("Seat ID in request cannot be null for this operation.");
        }

        // Если purchaseTime не приходит в DTO, возможно, нужно установить его здесь при создании
        // ticket.setPurchaseTime(LocalDateTime.now());

        return ticket;
    }

    @Transactional
    @Override
    public Ticket updateTicketFromRequest(Ticket existingTicket, TicketRequest ticketRequest) {
        logger.debug("Updating existing Ticket entity with data from TicketRequest DTO");
        // Handle null price from DTO (now it's Double)
        if (ticketRequest.getPrice() != null) {
            existingTicket.setPrice(BigDecimal.valueOf(ticketRequest.getPrice()));
        } else {
            existingTicket.setPrice(null); // Или установите значение по умолчанию/игнорируйте
            logger.warn("Price is null in TicketRequest for update.");
        }

        // Обновляем Showtime, если приходит новое ID
        if (ticketRequest.getShowtimeId() != null) {
            Showtime showtime = showtimeRepository.findById(ticketRequest.getShowtimeId())
                    .orElseThrow(() -> new RuntimeException("Showtime not found with ID: " + ticketRequest.getShowtimeId()));
            existingTicket.setShowtime(showtime);
        } else {
            existingTicket.setShowtime(null); // Или оставьте текущий, если null в DTO означает не обновлять
        }

        // Обновляем User, если приходит новое ID
        if (ticketRequest.getUserId() != null) {
            User user = userRepository.findById(ticketRequest.getUserId())
                    .orElseThrow(() -> new RuntimeException("User not found with ID: " + ticketRequest.getUserId()));
            existingTicket.setUser(user);
        } else {
            existingTicket.setUser(null); // Или оставьте текущего пользователя
        }

        // Обновляем Seat, если приходит новое ID
        if (ticketRequest.getSeatId() != null) {
            Seat seat = seatRepository.findById(ticketRequest.getSeatId())
                    .orElseThrow(() -> new RuntimeException("Seat not found with ID: " + ticketRequest.getSeatId()));
            existingTicket.setSeat(seat);
            // Если у Ticket есть отдельное поле seatNumber, обновите его из новой Seat сущности
            existingTicket.setSeatNumber(seat.getSeatRow() + "-" + seat.getNumber()); // Assuming Seat has getSeatRow() and getNumber()
        } else {
            existingTicket.setSeat(null); // Или оставьте текущее место
        }


        return existingTicket;
    }

    // TODO: Implement findTicketsByShowtimeDateTime, findTicketsBySeatId, findByShowtimeId, findByShowtimeAndSeatNumber methods with manual fetching and caching
}
