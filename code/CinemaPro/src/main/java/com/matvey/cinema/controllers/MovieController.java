package com.matvey.cinema.controllers;

import com.matvey.cinema.exception.CustomNotFoundException;
import com.matvey.cinema.model.dto.MovieRequest;
import com.matvey.cinema.model.entities.Movie;
import com.matvey.cinema.service.MovieService;

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
// import org.springframework.transaction.annotation.Transactional; // Not typically needed on controllers when service is transactional

@RestController
@RequestMapping("/api/movies")
@CrossOrigin(origins = "http://localhost:5173")
@Tag(name = "Movie Controller", description = "API для управления фильмами")
public class MovieController {
    private final MovieService movieService;
    private static final Logger logger = LoggerFactory.getLogger(MovieController.class);

    public MovieController(MovieService movieService) {
        this.movieService = movieService;
    }

    @GetMapping("/{id}")
    @Operation(summary = "Получить фильм по ID", description = "Возвращает фильм с указанным ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200",
                    description = "Фильм успешно получен",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = Movie.class))),
            @ApiResponse(responseCode = "404",
                    description = "Фильм не найден", content = @Content)
    })
    public ResponseEntity<Movie> getMovieById(
            @Parameter(description = "Идентификатор фильма", example = "1") @PathVariable Long id) {
        logger.debug("Запрос на получение фильма с ID: {}", id);
        Optional<Movie> movie = movieService.findById(id);
        return movie.map(ResponseEntity::ok)
                .orElseGet(() -> {
                    logger.error("Фильм с ID {} не найден", id);
                    return ResponseEntity.notFound().build();
                });
    }

    @GetMapping
    @Operation(summary = "Получить все фильмы",
            description = "Возвращает список всех фильмов в базе данных")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Список фильмов успешно получен",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = Movie.class)))
    })
    public ResponseEntity<List<Movie>> getAllMovies() {
        logger.debug("Запрос на получение всех фильмов с отзывами");
        List<Movie> movies = movieService.findAllWithReviews();
        return ResponseEntity.ok(movies);
    }

    @PostMapping
    @Operation(summary = "Создать новый фильм",
            description = "Создает новый фильм на основе предоставленных данных")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Фильм успешно создан",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = Movie.class))),
            @ApiResponse(responseCode = "400",
                    description = "Неверные входные данные", content = @Content)
    })
    public ResponseEntity<Movie> createMovie(@Valid @RequestBody MovieRequest movieRequest) {
        logger.debug("Запрос на создание нового фильма: {}", movieRequest);
        try {
            Movie createdMovie = movieService.createMovie(movieRequest);
            logger.info("Фильм с ID: {} успешно создан.", createdMovie.getId());
            return ResponseEntity.status(HttpStatus.CREATED).body(createdMovie);
        } catch (Exception e) {
            logger.error("Ошибка при создании фильма:", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PutMapping("/{id}")
    @Operation(summary = "Обновить фильм",
            description = "Обновляет существующий фильм с указанным ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Фильм успешно обновлен",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = Movie.class))),
            @ApiResponse(responseCode = "400",
                    description = "Неверные входные данные", content = @Content),
            @ApiResponse(responseCode = "404", description = "Фильм не найден", content = @Content)
    })
    // @Transactional // Transactionality should be handled by the service layer
    public ResponseEntity<Movie> updateMovie(
            @Parameter(description = "Идентификатор фильма для обновления",
                    example = "1") @PathVariable Long id,
            @Valid @RequestBody MovieRequest movieRequest) {
        logger.debug("Запрос на обновление фильма с ID: {}", id);
        try {
            // TODO: Implement updateMovie method in MovieService and call it here
            // Movie updatedMovie = movieService.updateMovie(id, movieRequest);
            // return ResponseEntity.ok(updatedMovie);

            // Temporary solution: find, update fields manually (not recommended)
            Movie existingMovie = movieService.findById(id)
                    .orElseThrow(() -> new CustomNotFoundException("Фильм не найден с ID: " + id));

            existingMovie.setTitle(movieRequest.getTitle());
            existingMovie.setDirector(movieRequest.getDirector());
            existingMovie.setReleaseYear(movieRequest.getReleaseYear());
            existingMovie.setGenre(movieRequest.getGenre());
            // TODO: Update reviews/showtimes collections here or in the updateMovie service method

            Movie updatedMovie = movieService.save(existingMovie); // Save the updated entity
            logger.info("Фильм с ID: {} успешно обновлен.", id);
            return ResponseEntity.ok(updatedMovie);


        } catch (CustomNotFoundException e) {
            logger.error("Фильм с ID {} не найден для обновления", id);
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            logger.error("Ошибка при обновлении фильма с ID {}:", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Удалить фильм", description = "Удаляет фильм с указанным ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204",
                    description = "Фильм успешно удален", content = @Content),
            @ApiResponse(responseCode = "404",
                    description = "Фильм не найден", content = @Content)
    })
    // @Transactional // Transactionality should be handled by the service layer
    public ResponseEntity<Void> deleteMovie(
            @Parameter(description = "Идентификатор фильма для удаления",
                    example = "1") @PathVariable Long id) {
        logger.debug("Запрос на удаление фильма с ID: {}", id);
        try {
            movieService.deleteById(id);
            logger.info("Фильм с ID: {} успешно удален.", id);
            return ResponseEntity.noContent().build();
        } catch (CustomNotFoundException e) {
            logger.error("Фильм с ID {} не найден для удаления", id);
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            logger.error("Ошибка при удалении фильма с ID {}:", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
