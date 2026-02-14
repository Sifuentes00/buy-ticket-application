package com.matvey.cinema.controllers;

import com.matvey.cinema.model.dto.TheaterRequest;
import com.matvey.cinema.model.entities.Theater;
import com.matvey.cinema.repository.TheaterRepository;
import com.matvey.cinema.service.SeatService;
import com.matvey.cinema.service.ShowtimeService;
import com.matvey.cinema.service.TheaterService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
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
@Tag(name = "Theater Controller", description = "API для управления театрами")
@CrossOrigin(origins = "http://localhost:5173")
@RequestMapping("/api/theaters")
public class TheaterController {
    private final SeatService seatService;
    private final TheaterService theaterService;
    private final ShowtimeService showtimeService;
    private final TheaterRepository theaterRepository;
    private static final Logger logger = LoggerFactory.getLogger(TheaterController.class);

    public TheaterController(TheaterService theaterService, SeatService seatService,
                             ShowtimeService showtimeService, TheaterRepository theaterRepository) {
        this.theaterService = theaterService;
        this.seatService = seatService;
        this.showtimeService = showtimeService;
        this.theaterRepository = theaterRepository;
    }

    @GetMapping("/{id}")
    @Operation(summary = "Получить театр по ID",
            description = "Возвращает театр с указанным ID")
    public ResponseEntity<Theater> getTheaterById(
            @Parameter(description = "Идентификатор театра", example = "1") @PathVariable Long id) {
        logger.debug("Запрос на получение театра с ID: {}", id);
        Optional<Theater> theater = theaterService.findById(id);
        return theater.map(ResponseEntity::ok)
                .orElseGet(() -> {
                    logger.error("Театр с ID {} не найден", id);
                    return ResponseEntity.notFound().build();
                });
    }

    @GetMapping
    @Operation(summary = "Получить все театры",
            description = "Возвращает список всех театров в базе данных")
    public ResponseEntity<List<Theater>> getAllTheaters() {
        logger.debug("Запрос на получение всех театров");
        List<Theater> theaters = theaterService.findAll();
        return ResponseEntity.ok(theaters);
    }

    @PostMapping
    @Operation(summary = "Создать новый театр",
            description = "Создает новый театр на основе предоставленных данных")
    public ResponseEntity<Theater> createTheater(@Valid @RequestBody TheaterRequest
                                                             theaterRequest) {
        logger.debug("Запрос на создание нового театра: {}", theaterRequest);
        Theater theater = new Theater();

        theaterRepository.updateTheaterDetails(theater, theaterRequest,
                seatService, showtimeService);

        Theater createdTheater = theaterService.save(theater);
        logger.info("Театр успешно создан с ID: {}", createdTheater.getId());

        return ResponseEntity.status(HttpStatus.CREATED).body(createdTheater);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Обновить театр",
            description = "Обновляет существующий театр с указанным ID")
    public ResponseEntity<Theater> updateTheater(
            @Parameter(description = "Идентификатор театра для обновления",
                    example = "1") @PathVariable Long id,
            @Valid @RequestBody TheaterRequest theaterRequest) {
        logger.debug("Запрос на обновление театра с ID: {}", id);
        Optional<Theater> theaterOptional = theaterService.findById(id);
        if (!theaterOptional.isPresent()) {
            logger.error("Театр с ID {} не найден", id);
            return ResponseEntity.notFound().build();
        }

        Theater theater = theaterOptional.get();
        theaterRepository.updateTheaterDetails(theater, theaterRequest,
                seatService, showtimeService);

        Theater updatedTheater = theaterService.save(theater);
        logger.info("Театр с ID: {} успешно обновлен", id);

        return ResponseEntity.ok(updatedTheater);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Удалить театр",
            description = "Удаляет театр с указанным ID")
    public ResponseEntity<Void> deleteTheater(
            @Parameter(description = "Идентификатор театра для удаления",
                    example = "1") @PathVariable Long id) {
        logger.debug("Запрос на удаление театра с ID: {}", id);
        theaterService.deleteById(id);
        logger.info("Театр с ID: {} успешно удален", id);
        return ResponseEntity.noContent().build();
    }
}
