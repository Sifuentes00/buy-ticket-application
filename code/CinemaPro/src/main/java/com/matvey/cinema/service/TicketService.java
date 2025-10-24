package com.matvey.cinema.service;

import com.matvey.cinema.model.dto.PurchaseRequestDto;
import com.matvey.cinema.model.dto.TicketRequest;
import com.matvey.cinema.model.entities.Showtime; // Keep if needed for findByShowtimeAndSeatNumber signature
import com.matvey.cinema.model.entities.Ticket;
import java.util.List;
import java.util.Optional;

public interface TicketService {

    List<Ticket> findAll();

    Optional<Ticket> findById(Long id);

    List<Ticket> findByUserId(Long userId);

    List<Ticket> findTicketsByUserUsername(String userUsername);

    List<Ticket> findTicketsByShowtimeDateTime(String showtimeDateTime);

    List<Ticket> findTicketsBySeatId(Long seatId);

    List<Ticket> findByShowtimeId(Long showtimeId);

    Optional<Ticket> findByShowtimeAndSeatNumber(Showtime showtime, String seatNumber);

    Ticket save(Ticket ticket);

    void deleteById(Long id);

    Ticket updateTicketFromRequest(Ticket existingTicket, TicketRequest ticketRequest);

    Ticket mapTicketRequestToTicket(TicketRequest ticketRequest);

    List<Ticket> purchaseTickets(PurchaseRequestDto purchaseRequest);
}