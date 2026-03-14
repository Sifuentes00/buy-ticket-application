package com.matvey.cinema.service.impl;

import com.matvey.cinema.cache.CacheKeys;
import com.matvey.cinema.cache.InMemoryCache;
import com.matvey.cinema.model.dto.PurchaseRequestDto;
import com.matvey.cinema.model.dto.TicketRequest;
import com.matvey.cinema.model.entities.Seat;
import com.matvey.cinema.model.entities.Showtime;
import com.matvey.cinema.model.entities.Ticket;
import com.matvey.cinema.repository.SeatRepository;
import com.matvey.cinema.repository.ShowtimeRepository;
import com.matvey.cinema.repository.TicketRepository;
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

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;

@Service
public class TicketServiceImpl implements TicketService {

    private static final Logger logger = LoggerFactory.getLogger(TicketServiceImpl.class);

    private final TicketRepository ticketRepository;
    private final ShowtimeRepository showtimeRepository;
    private final SeatRepository seatRepository;
    private final InMemoryCache cache;

    private static final Pattern SEAT_PATTERN = Pattern.compile("(\\d+)-(\\d+)");

    @Autowired
    public TicketServiceImpl(TicketRepository ticketRepository,
                             ShowtimeRepository showtimeRepository,
                             SeatRepository seatRepository,
                             InMemoryCache cache) {

        this.ticketRepository = ticketRepository;
        this.showtimeRepository = showtimeRepository;
        this.seatRepository = seatRepository;
        this.cache = cache;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Ticket> findById(Long id) {

        String cacheKey = CacheKeys.TICKET_PREFIX + id;

        logger.info("event=ticket_find_by_id_start ticketId={} cacheKey={}", id, cacheKey);

        Optional<Object> cachedData = cache.get(cacheKey);

        if (cachedData.isPresent()) {

            logger.debug("event=cache_hit cacheKey={} ticketId={}", cacheKey, id);

            Object data = cachedData.get();

            if (data instanceof Ticket) {

                Ticket ticket = (Ticket) data;

                if (ticket.getShowtime() != null) {
                    ticket.getShowtime().getMovie();
                }

                if (ticket.getSeat() != null) {
                    ticket.getSeat();
                }

                return Optional.of(ticket);

            } else {

                cache.evict(cacheKey);

                logger.warn("event=cache_invalid_type cacheKey={}", cacheKey);
            }
        }

        Optional<Ticket> ticket = ticketRepository.findById(id);

        if (ticket.isEmpty()) {

            logger.warn("event=ticket_not_found ticketId={}", id);

            return Optional.empty();
        }

        ticket.ifPresent(value -> {

            if (value.getShowtime() != null) {
                value.getShowtime().getMovie();
            }

            if (value.getSeat() != null) {
                value.getSeat();
            }

            cache.put(cacheKey, value);

            logger.debug("event=cache_put cacheKey={} ticketId={}", cacheKey, id);
        });

        logger.info("event=ticket_find_by_id_success ticketId={}", id);

        return ticket;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Ticket> findAll() {

        String cacheKey = CacheKeys.TICKETS_ALL;

        logger.info("event=ticket_find_all_start");

        Optional<Object> cachedData = cache.get(cacheKey);

        if (cachedData.isPresent()) {

            logger.debug("event=cache_hit cacheKey={}", cacheKey);

            Object data = cachedData.get();

            if (data instanceof List) {

                List<?> list = (List<?>) data;

                if (list.isEmpty() || list.get(0) instanceof Ticket) {

                    try {

                        List<Ticket> tickets = (List<Ticket>) data;

                        tickets.forEach(ticket -> {

                            if (ticket.getShowtime() != null) {
                                ticket.getShowtime().getMovie();
                            }

                            if (ticket.getSeat() != null) {
                                ticket.getSeat();
                            }
                        });

                        return tickets;

                    } catch (ClassCastException e) {

                        logger.error("event=cache_cast_error cacheKey={}", cacheKey, e);

                        cache.evict(cacheKey);
                    }
                }
            }

            cache.evict(cacheKey);

            logger.warn("event=cache_invalid_type cacheKey={}", cacheKey);
        }

        List<Ticket> tickets = ticketRepository.findAll();

        tickets.forEach(ticket -> {

            if (ticket.getShowtime() != null) {
                ticket.getShowtime().getMovie();
            }

            if (ticket.getSeat() != null) {
                ticket.getSeat();
            }
        });

        cache.put(cacheKey, tickets);

        logger.debug("event=cache_put cacheKey={} count={}", cacheKey, tickets.size());

        logger.info("event=ticket_find_all_success count={}", tickets.size());

        return tickets;
    }

    @Transactional(readOnly = true)
    public List<Ticket> findMyTickets() {

        Jwt jwt = (Jwt) SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getPrincipal();

        String keycloakUserId = jwt.getSubject();

        logger.info("event=ticket_find_my_tickets userId={}", keycloakUserId);

        return ticketRepository.findMyTicketsWithMovie(keycloakUserId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Ticket> findTicketsByShowtimeDateTime(String showtimeDateTime) {

        String cacheKey = CacheKeys.TICKETS_SHOWTIME_PREFIX + showtimeDateTime;

        logger.info("event=ticket_find_by_showtime_datetime_start showtimeDateTime={} cacheKey={}",
                showtimeDateTime, cacheKey);

        Optional<Object> cachedData = cache.get(cacheKey);

        if (cachedData.isPresent()) {

            logger.debug("event=cache_hit cacheKey={} showtimeDateTime={}", cacheKey, showtimeDateTime);

            Object data = cachedData.get();

            if (data instanceof List) {

                List<?> list = (List<?>) data;

                if (list.isEmpty() || list.get(0) instanceof Ticket) {

                    try {

                        List<Ticket> tickets = (List<Ticket>) data;

                        tickets.forEach(ticket -> {

                            if (ticket.getShowtime() != null) {
                                ticket.getShowtime().getMovie();
                            }

                            if (ticket.getSeat() != null) {
                                ticket.getSeat();
                            }
                        });

                        return tickets;

                    } catch (ClassCastException e) {

                        logger.error("event=cache_cast_error cacheKey={}", cacheKey, e);

                        cache.evict(cacheKey);
                    }
                }
            }

            cache.evict(cacheKey);

            logger.warn("event=cache_invalid_type cacheKey={}", cacheKey);
        }

        List<Ticket> tickets = ticketRepository.findByShowtime_DateTime(showtimeDateTime);

        tickets.forEach(ticket -> {

            if (ticket.getShowtime() != null) {
                ticket.getShowtime().getMovie();
            }

            if (ticket.getSeat() != null) {
                ticket.getSeat();
            }
        });

        cache.put(cacheKey, tickets);

        logger.debug("event=cache_put cacheKey={} count={}", cacheKey, tickets.size());

        logger.info("event=ticket_find_by_showtime_datetime_success count={}", tickets.size());

        return tickets;
    }

    @Override
    public Optional<Ticket> findByShowtimeAndSeatNumber(Showtime showtime, String seatNumber) {

        logger.debug("event=ticket_find_by_showtime_and_seat showtimeId={} seatNumber={}",
                showtime.getId(), seatNumber);

        Matcher matcher = SEAT_PATTERN.matcher(seatNumber);

        if (!matcher.matches()) {

            logger.warn("event=seat_format_invalid seatNumber={}", seatNumber);

            return Optional.empty();
        }

        int row = Integer.parseInt(matcher.group(1));
        int seat = Integer.parseInt(matcher.group(2));

        Optional<Seat> seatOpt =
                seatRepository.findBySeatRowAndNumber(
                        row,
                        seat
                );

        if (seatOpt.isEmpty()) {

            logger.warn("event=seat_not_found row={} seat={} showtimeId={}",
                    row, seat, showtime.getId());

            return Optional.empty();
        }

        return ticketRepository.findByShowtimeAndSeat(showtime, seatOpt.get());
    }

    @Override
    @Transactional(readOnly = true)
    public List<Ticket> findTicketsBySeatId(Long seatId) {

        String cacheKey = CacheKeys.TICKETS_SEAT_PREFIX + seatId;

        logger.info("event=ticket_find_by_seat_start seatId={} cacheKey={}", seatId, cacheKey);

        Optional<Object> cachedData = cache.get(cacheKey);

        if (cachedData.isPresent()) {

            logger.debug("event=cache_hit cacheKey={} seatId={}", cacheKey, seatId);

            Object data = cachedData.get();

            if (data instanceof List) {

                List<?> list = (List<?>) data;

                if (list.isEmpty() || list.get(0) instanceof Ticket) {

                    try {

                        List<Ticket> tickets = (List<Ticket>) data;

                        tickets.forEach(ticket -> {

                            if (ticket.getShowtime() != null) {
                                ticket.getShowtime().getMovie();
                            }

                            if (ticket.getSeat() != null) {
                                ticket.getSeat();
                            }
                        });

                        return tickets;

                    } catch (ClassCastException e) {

                        logger.error("event=cache_cast_error cacheKey={}", cacheKey, e);

                        cache.evict(cacheKey);
                    }
                }
            }

            cache.evict(cacheKey);

            logger.warn("event=cache_invalid_type cacheKey={}", cacheKey);
        }

        List<Ticket> tickets = ticketRepository.findBySeat_Id(seatId);

        tickets.forEach(ticket -> {

            if (ticket.getShowtime() != null) {
                ticket.getShowtime().getMovie();
            }

            if (ticket.getSeat() != null) {
                ticket.getSeat();
            }
        });

        cache.put(cacheKey, tickets);

        logger.debug("event=cache_put cacheKey={} count={}", cacheKey, tickets.size());

        logger.info("event=ticket_find_by_seat_success seatId={} count={}", seatId, tickets.size());

        return tickets;
    }

    @Transactional
    @Override
    public Ticket save(Ticket ticket) {

        logger.info("event=ticket_save_start ticketId={}", ticket.getId());

        Ticket savedTicket = ticketRepository.save(ticket);

        logger.info("event=ticket_save_success ticketId={}", savedTicket.getId());

        cache.evict(CacheKeys.TICKETS_ALL);

        if (savedTicket.getId() != null) {
            cache.evict(CacheKeys.TICKET_PREFIX + savedTicket.getId());
        }

        if (savedTicket.getKeycloakUserId() != null) {

            cache.evict(CacheKeys.TICKETS_USER_PREFIX + savedTicket.getKeycloakUserId());

            logger.debug("event=cache_evict_user userId={}", savedTicket.getKeycloakUserId());
        }

        Optional.ofNullable(savedTicket.getShowtime())
                .map(Showtime::getId)
                .ifPresent(showtimeId -> {

                    cache.evict(CacheKeys.TICKETS_SHOWTIME_PREFIX + showtimeId);

                    logger.debug("event=cache_evict_showtime showtimeId={}", showtimeId);
                });

        Optional.ofNullable(savedTicket.getSeat())
                .map(Seat::getId)
                .ifPresent(seatId -> {

                    cache.evict(CacheKeys.TICKETS_SEAT_PREFIX + seatId);

                    logger.debug("event=cache_evict_seat seatId={}", seatId);
                });

        return savedTicket;
    }

    public void deleteTicket(Long id) {

        Jwt jwt = (Jwt) SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getPrincipal();

        String keycloakUserId = jwt.getSubject();

        logger.info("event=ticket_delete_user_start ticketId={} userId={}", id, keycloakUserId);

        Ticket ticket = ticketRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Ticket not found"));

        if (!ticket.getKeycloakUserId().equals(keycloakUserId)) {

            logger.warn("event=ticket_delete_access_denied ticketId={} userId={}", id, keycloakUserId);

            throw new RuntimeException("Access denied");
        }

        ticketRepository.delete(ticket);

        logger.info("event=ticket_delete_user_success ticketId={} userId={}", id, keycloakUserId);
    }

    @Transactional
    @Override
    public void deleteById(Long id) {

        logger.info("event=ticket_delete_start ticketId={}", id);

        Optional<Ticket> ticketOpt = ticketRepository.findById(id);

        if (ticketOpt.isEmpty()) {

            logger.warn("event=ticket_delete_not_found ticketId={}", id);

            throw new RuntimeException("Ticket not found with ID: " + id);
        }

        Ticket ticket = ticketOpt.get();

        cache.evict(CacheKeys.TICKETS_ALL);
        cache.evict(CacheKeys.TICKET_PREFIX + ticket.getId());

        if (ticket.getKeycloakUserId() != null) {
            cache.evict(CacheKeys.TICKETS_USER_PREFIX + ticket.getKeycloakUserId());
        }

        Optional.ofNullable(ticket.getShowtime())
                .map(Showtime::getId)
                .ifPresent(showtimeId -> {

                    cache.evict(CacheKeys.TICKETS_SHOWTIME_PREFIX + showtimeId);

                    logger.debug("event=cache_evict_showtime showtimeId={}", showtimeId);
                });

        Optional.ofNullable(ticket.getSeat())
                .map(Seat::getId)
                .ifPresent(seatId -> {

                    cache.evict(CacheKeys.TICKETS_SEAT_PREFIX + seatId);

                    logger.debug("event=cache_evict_seat seatId={}", seatId);
                });

        ticketRepository.deleteById(id);

        logger.info("event=ticket_delete_success ticketId={}", id);
    }

    @Transactional(readOnly = true)
    public List<Ticket> findByShowtimeId(Long showtimeId) {

        String cacheKey = CacheKeys.TICKETS_SHOWTIME_PREFIX + showtimeId;

        logger.info("event=ticket_find_by_showtime_start showtimeId={} cacheKey={}", showtimeId, cacheKey);

        Optional<Object> cachedData = cache.get(cacheKey);

        if (cachedData.isPresent()) {

            logger.debug("event=cache_hit cacheKey={} showtimeId={}", cacheKey, showtimeId);

            Object data = cachedData.get();

            if (data instanceof List) {

                List<?> list = (List<?>) data;

                if (list.isEmpty() || list.get(0) instanceof Ticket) {

                    try {

                        List<Ticket> tickets = (List<Ticket>) data;

                        tickets.forEach(ticket -> {

                            if (ticket.getShowtime() != null) {
                                ticket.getShowtime().getMovie();
                            }

                            if (ticket.getSeat() != null) {
                                ticket.getSeat();
                            }
                        });

                        return tickets;

                    } catch (ClassCastException e) {

                        logger.error("event=cache_cast_error cacheKey={}", cacheKey, e);

                        cache.evict(cacheKey);
                    }
                }
            }

            cache.evict(cacheKey);

            logger.warn("event=cache_invalid_type cacheKey={}", cacheKey);
        }

        List<Ticket> tickets = ticketRepository.findByShowtimeId(showtimeId);

        tickets.forEach(ticket -> {

            if (ticket.getShowtime() != null) {
                ticket.getShowtime().getMovie();
            }

            if (ticket.getSeat() != null) {
                ticket.getSeat();
            }
        });

        cache.put(cacheKey, tickets);

        logger.debug("event=cache_put cacheKey={} count={}", cacheKey, tickets.size());

        logger.info("event=ticket_find_by_showtime_success showtimeId={} count={}",
                showtimeId, tickets.size());

        return tickets;
    }

    @Transactional
    @Override
    public List<Ticket> purchaseTickets(PurchaseRequestDto purchaseRequest) {

        Jwt jwt = (Jwt) SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getPrincipal();

        String keycloakUserId = jwt.getSubject();

        logger.info("event=ticket_purchase_start userId={} showtimeId={} seats={}",
                keycloakUserId,
                purchaseRequest.getShowtimeId(),
                purchaseRequest.getSeatNumbers());

        Showtime showtime = showtimeRepository
                .findById(purchaseRequest.getShowtimeId())
                .orElseThrow(() -> new RuntimeException("Showtime not found"));

        List<Ticket> purchasedTickets = new ArrayList<>();

        BigDecimal ticketPrice = showtime.getPrice();

        for (String seatNumber : purchaseRequest.getSeatNumbers()) {

            Matcher matcher = SEAT_PATTERN.matcher(seatNumber);

            if (!matcher.matches()) {

                logger.warn("event=seat_format_invalid seatNumber={} userId={}",
                        seatNumber, keycloakUserId);

                throw new RuntimeException("Invalid seat format: " + seatNumber);
            }

            int row = Integer.parseInt(matcher.group(1));
            int seat = Integer.parseInt(matcher.group(2));

            Seat seatEntity = seatRepository
                    .findBySeatRowAndNumber(row, seat)
                    .orElseThrow(() ->
                            new RuntimeException("Seat not found: " + seatNumber));

            Optional<Ticket> existingTicket =
                    ticketRepository.findByShowtimeAndSeat(showtime, seatEntity);

            if (existingTicket.isPresent()) {

                logger.warn("event=seat_already_booked seatNumber={} showtimeId={}",
                        seatNumber, showtime.getId());

                throw new RuntimeException("Seat already booked: " + seatNumber);
            }

            Ticket ticket = new Ticket();
            ticket.setShowtime(showtime);
            ticket.setSeat(seatEntity);
            ticket.setKeycloakUserId(keycloakUserId);
            ticket.setPrice(ticketPrice);

            Ticket savedTicket = ticketRepository.save(ticket);

            purchasedTickets.add(savedTicket);

            logger.info("event=ticket_created ticketId={} seat={} userId={}",
                    savedTicket.getId(), seatNumber, keycloakUserId);
        }

        cache.evict(CacheKeys.TICKETS_ALL);
        cache.evict(CacheKeys.TICKETS_SHOWTIME_PREFIX + showtime.getId());
        cache.evict(CacheKeys.TICKETS_USER_PREFIX + keycloakUserId);

        logger.debug("event=cache_evict_purchase showtimeId={} userId={}",
                showtime.getId(), keycloakUserId);

        logger.info("event=ticket_purchase_success userId={} ticketsCount={}",
                keycloakUserId, purchasedTickets.size());

        return purchasedTickets;
    }

    public Ticket mapTicketRequestToTicket(TicketRequest request) {

        logger.debug("event=ticket_mapping_request request={}", request);

        Ticket ticket = new Ticket();

        updateTicketFromRequest(ticket, request);

        return ticket;
    }

    public Ticket updateTicketFromRequest(Ticket ticket, TicketRequest request) {

        logger.debug("event=ticket_update_from_request ticketId={}", ticket.getId());

        Showtime showtime = showtimeRepository
                .findById(request.getShowtimeId())
                .orElseThrow(() -> new RuntimeException("Showtime not found"));

        Seat seat = seatRepository
                .findById(request.getSeatId())
                .orElseThrow(() -> new RuntimeException("Seat not found"));

        ticket.setShowtime(showtime);
        ticket.setSeat(seat);

        return ticket;
    }
}
