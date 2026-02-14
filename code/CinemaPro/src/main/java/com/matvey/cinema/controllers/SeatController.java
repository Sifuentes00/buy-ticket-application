package com.matvey.cinema.controllers;

import com.matvey.cinema.model.dto.SeatRequest;
import com.matvey.cinema.model.entities.Seat;
import com.matvey.cinema.repository.SeatRepository;
import com.matvey.cinema.service.SeatService;
import com.matvey.cinema.service.TheaterService;
import com.matvey.cinema.service.TicketService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/seats")
@CrossOrigin(origins = "http://localhost:5173")
@Tag(name = "Seat Controller", description = "API для управления местами")
public class SeatController {
    private final SeatRepository seatRepository;
    private final SeatService seatService;
    private final TheaterService theaterService;
    private final TicketService ticketService;
    private static final Logger logger = LoggerFactory.getLogger(SeatController.class);

    public SeatController(SeatService seatService, TheaterService theaterService,
                          TicketService ticketService, SeatRepository seatRepository) {
        this.seatService = seatService;
        this.theaterService = theaterService;
        this.ticketService = ticketService;
        this.seatRepository = seatRepository;
    }

    @GetMapping("/{id}")
    @Operation(summary = "Получить место по ID", description = "Возвращает место с указанным ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Место успешно получено",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = Seat.class))),
        @ApiResponse(responseCode = "404", description = "Место не найдено", content = @Content)
    })
    public ResponseEntity<Seat> getSeatById(
            @Parameter(description = "Идентификатор места", example = "1") @PathVariable Long id) {
        logger.debug("Запрос на получение места с ID: {}", id);
        Optional<Seat> seat = seatService.findById(id);
        return seat.map(ResponseEntity::ok)
                .orElseGet(() -> {
                    logger.error("Место с ID {} не найдено", id);
                    return ResponseEntity.notFound().build();
                });
    }

    @GetMapping
    @Operation(summary = "Получить все места",
            description = "Возвращает список всех мест в базе данных")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Список мест успешно получен",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = Seat.class)))
    })
    public ResponseEntity<List<Seat>> getAllSeats() {
        logger.debug("Запрос на получение всех мест");
        List<Seat> seats = seatService.findAll();
        return ResponseEntity.ok(seats);
    }

    @GetMapping("/theater")
    @Operation(summary = "Получить места по названию театра",
            description = "Возвращает список мест для указанного театра")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Список мест успешно получен",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = Seat.class))),
        @ApiResponse(responseCode = "204", description = "Места не найдены", content = @Content)
    })
    public ResponseEntity<List<Seat>> getSeatsByTheaterId(
            @RequestParam String theaterName) {
        logger.debug("Запрос на получение мест для театра");
        List<Seat> seats = seatService.findSeatsByTheaterName(theaterName);
        if (seats.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(seats);
    }

    @PostMapping
    @Operation(summary = "Создать новое место",
            description = "Создает новое место на основе предоставленных данных")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201",
                description = "Место успешно создано",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = Seat.class))),
        @ApiResponse(responseCode = "400",
                description = "Неверные входные данные", content = @Content)
    })
    public ResponseEntity<Seat> createSeat(@Valid @RequestBody SeatRequest seatRequest) {
        logger.debug("Запрос на создание нового места: {}", seatRequest);
        Seat seat = new Seat();
        seatRepository.updateSeatDetails(seat, seatRequest, theaterService, ticketService);
        Seat createdSeat = seatService.save(seat);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdSeat);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Обновить место",
            description = "Обновляет существующее место с указанным ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Место успешно обновлено",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = Seat.class))),
        @ApiResponse(responseCode = "404", description = "Место не найдено", content = @Content)
    })
    public ResponseEntity<Seat> updateSeatWithTheaterAndTickets(
            @Parameter(description = "Идентификатор места для обновления",
                    example = "1") @PathVariable Long id,
            @Valid @RequestBody SeatRequest seatRequest) {
        logger.debug("Запрос на обновление места с ID: {}", id);
        Optional<Seat> existingSeat = seatService.findById(id);
        if (existingSeat.isPresent()) {
            seatRepository.updateSeatDetails(existingSeat.get(), seatRequest,
                    theaterService, ticketService);
            Seat updatedSeat = seatService.save(existingSeat.get());
            return ResponseEntity.ok(updatedSeat);
        } else {
            logger.error("Место с ID {} не найдено", id);
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Удалить место", description = "Удаляет место с указанным ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204",
                description = "Место успешно удалено", content = @Content),
        @ApiResponse(responseCode = "404",
                description = "Место не найдено", content = @Content)
    })
    public ResponseEntity<Void> deleteSeat(
            @Parameter(description = "Идентификатор места для удаления",
                    example = "1") @PathVariable Long id) {
        logger.debug("Запрос на удаление места с ID: {}", id);
        seatService.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
