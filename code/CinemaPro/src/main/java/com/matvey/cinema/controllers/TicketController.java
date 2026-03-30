package com.matvey.cinema.controllers;

import com.matvey.cinema.model.dto.PurchaseRequestDto;
import com.matvey.cinema.model.dto.TicketRequest;
import com.matvey.cinema.model.entities.Ticket;
import com.matvey.cinema.service.TicketService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

@RestController
@Tag(name = "Ticket Controller", description = "API for managing tickets")
@RequestMapping("/api/tickets")
public class TicketController {

    private static final Logger logger = LoggerFactory.getLogger(TicketController.class);

    private final TicketService ticketService;

    @Autowired
    public TicketController(TicketService ticketService) {
        this.ticketService = ticketService;
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get ticket by ID")
    public ResponseEntity<Ticket> getTicketById(
            @Parameter(description = "Ticket ID") @PathVariable Long id) {

        logger.info("event=api_ticket_get_by_id_start ticketId={}", id);

        Optional<Ticket> ticket = ticketService.findById(id);

        if (ticket.isEmpty()) {
            logger.warn("event=api_ticket_not_found ticketId={}", id);
            return ResponseEntity.notFound().build();
        }

        logger.info("event=api_ticket_get_by_id_success ticketId={}", id);

        return ResponseEntity.ok(ticket.get());
    }

    @GetMapping
    @Operation(summary = "Get all tickets")
    public ResponseEntity<List<Ticket>> getAllTickets() {

        logger.info("event=api_ticket_get_all_start");

        List<Ticket> tickets = ticketService.findAll();

        logger.info("event=api_ticket_get_all_success count={}", tickets.size());

        return ResponseEntity.ok(tickets);
    }

    @GetMapping("/showtime_datetime")
    @Operation(summary = "Get tickets by showtime datetime")
    public ResponseEntity<List<Ticket>> getTicketsByShowtimeDateTime(
            @Parameter(description = "Showtime datetime") @RequestParam String showtimeDateTime) {

        logger.info("event=api_ticket_get_by_showtime_datetime_start datetime={}", showtimeDateTime);

        List<Ticket> tickets = ticketService.findTicketsByShowtimeDateTime(showtimeDateTime);

        if (tickets.isEmpty()) {
            logger.warn("event=api_ticket_not_found_showtime_datetime datetime={}", showtimeDateTime);
            return ResponseEntity.noContent().build();
        }

        logger.info("event=api_ticket_get_by_showtime_datetime_success datetime={} count={}",
                showtimeDateTime, tickets.size());

        return ResponseEntity.ok(tickets);
    }

    @GetMapping("/seat/{seatId}")
    @Operation(summary = "Get tickets by Seat ID")
    public ResponseEntity<List<Ticket>> getTicketsBySeatId(
            @Parameter(description = "Seat ID") @PathVariable Long seatId) {

        logger.info("event=api_ticket_get_by_seat_start seatId={}", seatId);

        List<Ticket> tickets = ticketService.findTicketsBySeatId(seatId);

        if (tickets.isEmpty()) {
            logger.warn("event=api_ticket_not_found_seat seatId={}", seatId);
            return ResponseEntity.noContent().build();
        }

        logger.info("event=api_ticket_get_by_seat_success seatId={} count={}",
                seatId, tickets.size());

        return ResponseEntity.ok(tickets);
    }

    @GetMapping("/showtime/{showtimeId}")
    @Operation(summary = "Get tickets by Showtime ID")
    public ResponseEntity<List<Ticket>> getTicketsByShowtimeId(
            @Parameter(description = "Showtime ID") @PathVariable Long showtimeId) {

        logger.info("event=api_ticket_get_by_showtime_start showtimeId={}", showtimeId);

        List<Ticket> tickets = ticketService.findByShowtimeId(showtimeId);

        if (tickets.isEmpty()) {
            logger.warn("event=api_ticket_not_found_showtime showtimeId={}", showtimeId);
            return ResponseEntity.noContent().build();
        }

        logger.info("event=api_ticket_get_by_showtime_success showtimeId={} count={}",
                showtimeId, tickets.size());

        return ResponseEntity.ok(tickets);
    }

    @GetMapping("/my")
    public ResponseEntity<List<Ticket>> getMyTickets() {

        logger.info("event=api_ticket_get_my_start");

        List<Ticket> tickets = ticketService.findMyTickets();

        logger.info("event=api_ticket_get_my_success count={}", tickets.size());

        return ResponseEntity.ok(tickets);
    }

    @PostMapping
    @Operation(summary = "Create a new ticket (single)")
    public ResponseEntity<Ticket> createTicket(@Valid @RequestBody TicketRequest ticketRequest) {

        logger.info("event=api_ticket_create_start request={}", ticketRequest);

        try {

            Ticket ticketToSave = ticketService.mapTicketRequestToTicket(ticketRequest);
            Ticket savedTicket = ticketService.save(ticketToSave);

            logger.info("event=api_ticket_create_success ticketId={}", savedTicket.getId());

            return ResponseEntity.status(HttpStatus.CREATED).body(savedTicket);

        } catch (RuntimeException e) {

            logger.error("event=api_ticket_create_error message={}", e.getMessage());

            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);

        } catch (Exception e) {

            logger.error("event=api_ticket_create_error_unexpected", e);

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update ticket")
    public ResponseEntity<Ticket> updateTicket(
            @Parameter(description = "Ticket ID") @PathVariable Long id,
            @Valid @RequestBody TicketRequest ticketRequest) {

        logger.info("event=api_ticket_update_start ticketId={}", id);

        try {

            Optional<Ticket> ticketOpt = ticketService.findById(id);

            if (ticketOpt.isEmpty()) {

                logger.warn("event=api_ticket_update_not_found ticketId={}", id);

                return ResponseEntity.notFound().build();
            }

            Ticket updatedTicket =
                    ticketService.updateTicketFromRequest(ticketOpt.get(), ticketRequest);

            Ticket savedTicket = ticketService.save(updatedTicket);

            logger.info("event=api_ticket_update_success ticketId={}", id);

            return ResponseEntity.ok(savedTicket);

        } catch (RuntimeException e) {

            logger.error("event=api_ticket_update_error ticketId={} message={}", id, e.getMessage());

            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);

        } catch (Exception e) {

            logger.error("event=api_ticket_update_error_unexpected ticketId={}", id, e);

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTicket(@PathVariable Long id) {

        logger.info("event=api_ticket_delete_start ticketId={}", id);

        ticketService.deleteTicket(id);

        logger.info("event=api_ticket_delete_success ticketId={}", id);

        return ResponseEntity.noContent().build();
    }

    @PostMapping("/purchase")
    @Operation(summary = "Purchase tickets")
    public ResponseEntity<List<Ticket>> purchaseTickets(
            @Valid @RequestBody PurchaseRequestDto purchaseRequest) {

        logger.info("event=api_ticket_purchase_start showtimeId={} seats={}",
                purchaseRequest.getShowtimeId(),
                purchaseRequest.getSeatNumbers());

        try {
            String userEmail = ((Jwt) SecurityContextHolder.getContext().getAuthentication().getPrincipal())
                    .getClaimAsString("email");
            List<Ticket> purchasedTickets =
                    ticketService.purchaseTickets(purchaseRequest);

            logger.info("event=api_ticket_purchase_success count={}",
                    purchasedTickets.size());

            return ResponseEntity.status(HttpStatus.CREATED).body(purchasedTickets);

        } catch (IllegalStateException e) {

            logger.warn("event=api_ticket_purchase_conflict message={}", e.getMessage());

            return ResponseEntity.status(HttpStatus.CONFLICT).body(null);

        } catch (IllegalArgumentException e) {

            logger.warn("event=api_ticket_purchase_bad_request message={}", e.getMessage());

            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);

        } catch (RuntimeException e) {

            logger.error("event=api_ticket_purchase_runtime_error message={}", e.getMessage());

            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);

        } catch (Exception e) {

            logger.error("event=api_ticket_purchase_error_unexpected", e);

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }
}