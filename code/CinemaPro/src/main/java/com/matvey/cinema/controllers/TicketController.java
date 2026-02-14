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
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@Tag(name = "Ticket Controller", description = "API for managing tickets")
@CrossOrigin(origins = "http://localhost:5173")
@RequestMapping("/api/tickets")
public class TicketController {
    private final TicketService ticketService;

    private static final Logger logger = LoggerFactory.getLogger(TicketController.class);

    @Autowired
    public TicketController(TicketService ticketService) {
        this.ticketService = ticketService;
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get ticket by ID")
    public ResponseEntity<Ticket> getTicketById(
            @Parameter(description = "Ticket ID") @PathVariable Long id) {
        logger.debug("Request to get ticket with ID: {}", id);
        Optional<Ticket> ticket = ticketService.findById(id);
        return ticket.map(ResponseEntity::ok)
                .orElseGet(() -> {
                    logger.warn("Ticket with ID {} not found", id);
                    return ResponseEntity.notFound().build();
                });
    }

    @GetMapping
    @Operation(summary = "Get all tickets")
    public ResponseEntity<List<Ticket>> getAllTickets() {
        logger.debug("Request to get all tickets");
        List<Ticket> tickets = ticketService.findAll();
        return ResponseEntity.ok(tickets);
    }

    @GetMapping("/user")
    @Operation(summary = "Get tickets by username")
    public ResponseEntity<List<Ticket>> getTicketsByUserUsername(
            @Parameter(description = "Username") @RequestParam String userUsername) {
        logger.debug("Request to get tickets for user {}", userUsername);
        List<Ticket> tickets = ticketService.findTicketsByUserUsername(userUsername);
        if (tickets.isEmpty()) {
            logger.debug("Tickets for user {} not found (returning 204)", userUsername);
            return ResponseEntity.noContent().build();
        }
        logger.debug("Found {} tickets for user {}", tickets.size(), userUsername);
        return ResponseEntity.ok(tickets);
    }

    @GetMapping("/showtime_datetime")
    @Operation(summary = "Get tickets by showtime datetime")
    public ResponseEntity<List<Ticket>> getTicketsByShowtimeDateTime(
            @Parameter(description = "Showtime datetime") @RequestParam String showtimeDateTime) {
        logger.debug("Request to get tickets for showtime datetime {}", showtimeDateTime);
        List<Ticket> tickets = ticketService.findTicketsByShowtimeDateTime(showtimeDateTime);
        if (tickets.isEmpty()) {
            logger.debug("Tickets for showtime datetime {} not found (returning 204)", showtimeDateTime);
            return ResponseEntity.noContent().build();
        }
        logger.debug("Found {} tickets for showtime datetime {}", tickets.size(), showtimeDateTime);
        return ResponseEntity.ok(tickets);
    }

    @GetMapping("/seat/{seatId}")
    @Operation(summary = "Get tickets by Seat ID")
    public ResponseEntity<List<Ticket>> getTicketsBySeatId(
            @Parameter(description = "Seat ID") @PathVariable Long seatId) {
        logger.debug("Request to get tickets for seat with ID: {}", seatId);
        List<Ticket> tickets = ticketService.findTicketsBySeatId(seatId);
        if (tickets.isEmpty()) {
            logger.debug("Tickets for seat with ID {} not found (returning 204)", seatId);
            return ResponseEntity.noContent().build();
        }
        logger.debug("Found {} tickets for seat with ID {}", tickets.size(), seatId);
        return ResponseEntity.ok(tickets);
    }

    @GetMapping("/showtime/{showtimeId}")
    @Operation(summary = "Get tickets by Showtime ID")
    public ResponseEntity<List<Ticket>> getTicketsByShowtimeId(@PathVariable Long showtimeId) {
        logger.debug("Request to get tickets for showtime with ID: {}", showtimeId);
        List<Ticket> tickets = ticketService.findByShowtimeId(showtimeId);
        if (tickets.isEmpty()) {
            logger.debug("Tickets for showtime with ID {} not found (returning 204)", showtimeId);
            return ResponseEntity.noContent().build();
        }
        logger.debug("Found {} tickets for showtime with ID {}", tickets.size(), showtimeId);
        return ResponseEntity.ok(tickets);
    }

    @GetMapping("/user/{userId}") // Новый эндпоинт
    @Operation(summary = "Get tickets by user ID",
            description = "Returns a list of all tickets for the specified user")
    public ResponseEntity<List<Ticket>> getTicketsByUserId(
            @Parameter(description = "ID of the user") @PathVariable Long userId) {
        logger.debug("Request to get tickets for user ID: {}", userId);
        List<Ticket> tickets = ticketService.findByUserId(userId); // Вызываем метод сервиса

        if (tickets.isEmpty()) {
            logger.debug("Tickets for user ID {} not found (returning 204)", userId);
            return ResponseEntity.noContent().build(); // 204 No Content if no tickets
        }

        logger.debug("Found {} tickets for user ID {}", tickets.size(), userId);
        return ResponseEntity.ok(tickets); // 200 OK with list of tickets
    }

    @PostMapping // Endpoint for creating a SINGLE ticket (if TicketRequest DTO is for this)
    @Operation(summary = "Create a new ticket (single)")
    public ResponseEntity<Ticket> createTicket(@Valid @RequestBody TicketRequest ticketRequest) {
        logger.debug("Request to create a new ticket (single): {}", ticketRequest);
        try {
            // Map DTO to entity and save via service
            Ticket ticketToSave = ticketService.mapTicketRequestToTicket(ticketRequest);
            Ticket savedTicket = ticketService.save(ticketToSave);

            logger.info("Single ticket successfully created with ID: {}", savedTicket.getId());
            return ResponseEntity.status(HttpStatus.CREATED).body(savedTicket);
        } catch (RuntimeException e) {
            // Catch RuntimeExceptions from service (e.g., resource not found, invalid IDs)
            logger.error("Error creating single ticket: {}", e.getMessage());
            // Map to 400 Bad Request or 404 Not Found depending on the cause
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        } catch (Exception e) {
            logger.error("Unexpected error while creating single ticket", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @PutMapping("/{id}") // Endpoint for updating a SINGLE ticket (if TicketRequest DTO is for this)
    @Operation(summary = "Update ticket (single)")
    public ResponseEntity<Ticket> updateTicket(
            @Parameter(description = "Ticket ID for update") @PathVariable Long id,
            @Valid @RequestBody TicketRequest ticketRequest) {
        logger.debug("Request to update ticket (single) with ID: {}", id);
        try {
            Optional<Ticket> ticketOpt = ticketService.findById(id);
            if (ticketOpt.isEmpty()) {
                logger.warn("Ticket with ID {} not found for update", id);
                return ResponseEntity.notFound().build();
            }
            Ticket existingTicket = ticketOpt.get();

            // Update existing entity from DTO and save via service
            Ticket updatedTicketEntity = ticketService.updateTicketFromRequest(existingTicket, ticketRequest);
            Ticket savedTicket = ticketService.save(updatedTicketEntity);

            logger.info("Ticket with ID: {} successfully updated", id);
            return ResponseEntity.ok(savedTicket);
        } catch (RuntimeException e) {
            // Catch RuntimeExceptions from service (e.g., resource not found, invalid IDs)
            logger.error("Error updating single ticket: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        } catch (Exception e) {
            logger.error("Unexpected error while updating single ticket", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete ticket")
    public ResponseEntity<Void> deleteTicket(
            @Parameter(description = "Ticket ID for deletion") @PathVariable Long id) {
        logger.debug("Request to delete ticket with ID: {}", id);
        try {
            ticketService.deleteById(id); // Delete via service
            logger.info("Ticket with ID: {} successfully deleted", id);
            return ResponseEntity.noContent().build(); // 204 No Content
        } catch (RuntimeException e) {
            // Catch RuntimeException if ticket not found
            logger.error("Error deleting ticket with ID {}: {}", id, e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        } catch (Exception e) {
            logger.error("Unexpected error while deleting ticket with ID {}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @PostMapping("/purchase") // Endpoint for purchasing MULTIPLE tickets
    @Operation(summary = "Purchase tickets")
    public ResponseEntity<List<Ticket>> purchaseTickets(@Valid @RequestBody PurchaseRequestDto purchaseRequest) {
        logger.debug("Request to purchase tickets: showtime ID: {}, user ID: {}, seats: {}",
                purchaseRequest.getShowtimeId(), purchaseRequest.getUserId(), purchaseRequest.getSeatNumbers().size());

        try {
            // Call the service method that handles purchase logic and transaction
            List<Ticket> purchasedTickets = ticketService.purchaseTickets(purchaseRequest);

            logger.info("Purchase successfully processed. Created tickets: {}", purchasedTickets.size());
            return ResponseEntity.status(HttpStatus.CREATED).body(purchasedTickets);

        } catch (IllegalStateException e) {
            // Catch IllegalStateException if a seat is occupied
            logger.warn("Purchase error: Seat occupied. {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT).body(null); // 409 Conflict
        } catch (IllegalArgumentException e) {
            // Catch IllegalArgumentException (e.g., invalid seat format)
            logger.warn("Purchase error: Invalid argument. {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null); // 400 Bad Request
        } catch (RuntimeException e) {
            // Catch other RuntimeExceptions (e.g., resource not found)
            logger.error("Purchase error: RuntimeException. {}", e.getMessage());
            // Map to 400 Bad Request or 404 Not Found depending on the cause
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null); // 400 Bad Request
        } catch (Exception e) {
            // Any other unexpected errors
            logger.error("Unexpected error during ticket purchase", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null); // 500 Internal Server Error
        }
    }
}