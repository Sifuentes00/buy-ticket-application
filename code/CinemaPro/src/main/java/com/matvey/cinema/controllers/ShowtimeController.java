package com.matvey.cinema.controllers;

import com.matvey.cinema.model.dto.ShowtimeRequest;
import com.matvey.cinema.model.entities.Showtime;
import com.matvey.cinema.repository.ShowtimeRepository;
import com.matvey.cinema.service.MovieService;
import com.matvey.cinema.service.ShowtimeService;
import com.matvey.cinema.service.TheaterService;
import com.matvey.cinema.service.TicketService;
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
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/showtimes")
@Tag(name = "Showtime Controller", description = "API для управления сеансами")
public class ShowtimeController {

    private static final Logger logger = LoggerFactory.getLogger(ShowtimeController.class);

    private final ShowtimeService showtimeService;
    private final MovieService movieService;
    private final TheaterService theaterService;
    private final TicketService ticketService;
    private final ShowtimeRepository showtimeRepository;

    public ShowtimeController(ShowtimeService showtimeService,
                              MovieService movieService,
                              TheaterService theaterService,
                              TicketService ticketService,
                              ShowtimeRepository showtimeRepository) {
        this.showtimeService = showtimeService;
        this.movieService = movieService;
        this.theaterService = theaterService;
        this.ticketService = ticketService;
        this.showtimeRepository = showtimeRepository;
    }

    @GetMapping("/{id}")
    @PreAuthorize("permitAll()")
    @Operation(summary = "Получить сеанс по ID", description = "Возвращает сеанс с указанным ID")
    public ResponseEntity<Showtime> getShowtimeById(
            @Parameter(description = "Идентификатор сеанса", example = "1") @PathVariable Long id) {
        logger.debug("showtime.getById start id={}", id);
        Optional<Showtime> showtime = showtimeService.findById(id);
        return showtime.map(ResponseEntity::ok)
                .orElseGet(() -> {
                    logger.warn("showtime.notFound id={}", id);
                    return ResponseEntity.notFound().build();
                });
    }

    @GetMapping
    @PreAuthorize("permitAll()")
    @Operation(summary = "Получить все сеансы", description = "Возвращает список всех сеансов")
    public ResponseEntity<List<Showtime>> getAllShowtimes() {
        logger.debug("showtime.getAll start");
        List<Showtime> showtimes = showtimeService.findAll();
        logger.info("showtime.getAll success count={}", showtimes.size());
        return ResponseEntity.ok(showtimes);
    }

    @GetMapping("/theater")
    @PreAuthorize("permitAll()")
    @Operation(summary = "Получить сеансы по названию театра", description = "Список сеансов для театра")
    public ResponseEntity<List<Showtime>> getShowtimesByTheaterName(@RequestParam String theaterName) {
        logger.debug("showtime.getByTheater start theater={}", theaterName);
        List<Showtime> showtimes = showtimeService.findShowtimesByTheaterName(theaterName);
        if (showtimes.isEmpty()) {
            logger.warn("showtime.notFound.theater theater={}", theaterName);
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(showtimes);
    }

    @GetMapping("/movie")
    @PreAuthorize("permitAll()")
    @Operation(summary = "Получить сеансы по названию фильма", description = "Список сеансов для фильма")
    public ResponseEntity<List<Showtime>> getShowtimesByMovieTitle(@RequestParam String movieTitle) {
        logger.debug("showtime.getByMovieTitle start title={}", movieTitle);
        List<Showtime> showtimes = showtimeService.findShowtimesByMovieTitle(movieTitle);
        if (showtimes.isEmpty()) {
            logger.warn("showtime.notFound.movieTitle title={}", movieTitle);
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(showtimes);
    }

    @GetMapping("/movie/{movieId}")
    @PreAuthorize("permitAll()")
    @Operation(summary = "Получить сеансы по ID фильма", description = "Список сеансов по ID фильма")
    public ResponseEntity<List<Showtime>> getShowtimesByMovieId(
            @Parameter(description = "ID фильма", example = "1") @PathVariable Long movieId) {
        logger.debug("showtime.getByMovieId start movieId={}", movieId);
        List<Showtime> showtimes = showtimeService.findShowtimesByMovieId(movieId);
        if (showtimes.isEmpty()) {
            logger.warn("showtime.notFound.movieId movieId={}", movieId);
            return ResponseEntity.ok(List.of());
        }
        logger.info("showtime.getByMovieId success movieId={} count={}", movieId, showtimes.size());
        return ResponseEntity.ok(showtimes);
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Создать сеанс", description = "Создает новый сеанс")
    public ResponseEntity<Showtime> createShowtime(@Valid @RequestBody ShowtimeRequest showtimeRequest) {
        logger.debug("showtime.create start request={}", showtimeRequest);
        Showtime showtime = new Showtime();
        showtimeRepository.updateShowtimeDetails(showtime, showtimeRequest,
                movieService, theaterService, ticketService);
        Showtime saved = showtimeService.save(showtime);
        logger.info("showtime.create success id={}", saved.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Обновить сеанс", description = "Обновляет существующий сеанс")
    public ResponseEntity<Showtime> updateShowtime(
            @Parameter(description = "ID сеанса", example = "1") @PathVariable Long id,
            @Valid @RequestBody ShowtimeRequest showtimeRequest) {
        logger.debug("showtime.update start id={}", id);
        Showtime existing = showtimeService.findById(id)
                .orElseThrow(() -> {
                    logger.warn("showtime.notFound id={}", id);
                    return new RuntimeException("Сеанс не найден с ID: " + id);
                });
        showtimeRepository.updateShowtimeDetails(existing, showtimeRequest,
                movieService, theaterService, ticketService);
        Showtime updated = showtimeService.save(existing);
        logger.info("showtime.update success id={}", id);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Удалить сеанс", description = "Удаляет сеанс по ID")
    public ResponseEntity<Void> deleteShowtime(
            @Parameter(description = "ID сеанса", example = "1") @PathVariable Long id) {
        logger.debug("showtime.delete start id={}", id);
        showtimeService.deleteById(id);
        logger.info("showtime.delete success id={}", id);
        return ResponseEntity.noContent().build();
    }
}