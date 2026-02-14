package com.matvey.cinema.controllers;

import com.matvey.cinema.model.dto.ReviewRequest;
import com.matvey.cinema.model.entities.Review;
import com.matvey.cinema.service.ReviewService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
// import org.springframework.transaction.annotation.Transactional; // Not typically needed on controllers when service is transactional

@RestController
@RequestMapping("/api/reviews")
@CrossOrigin(origins = "http://localhost:5173")
@Tag(name = "Review Controller", description = "API для управления отзывами")
public class ReviewController {
    private final ReviewService reviewService;
    private static final Logger logger = LoggerFactory.getLogger(ReviewController.class);

    public ReviewController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    @GetMapping
    @Operation(summary = "Получить все отзывы",
            description = "Возвращает список всех отзывов в базе данных")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Список отзывов успешно получен",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = Review.class)))
    })
    public ResponseEntity<List<Review>> getAllReviews() {
        logger.debug("Запрос на получение всех отзывов");
        List<Review> reviews = reviewService.findAll();
        return ResponseEntity.ok(reviews);
    }

    @GetMapping("/movie/{movieId}")
    public ResponseEntity<List<Review>> getReviewsByMovieId(
            @Parameter(description = "Идентификатор фильма", example = "1") @PathVariable Long movieId) {
        logger.debug("Запрос на получение отзывов для фильма с ID: {}", movieId);
        try {
            List<Review> reviews = reviewService.findReviewsByMovieId(movieId);

            if (reviews.isEmpty()) {
                return ResponseEntity.noContent().build();
            }
            return ResponseEntity.ok(reviews);
        } catch (Exception e) {
            logger.error("Ошибка при получении отзывов для фильма с ID {}:", movieId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/user/{userId}")
    @Operation(summary = "Get reviews by user ID",
            description = "Returns a list of all reviews for the specified user")
    public ResponseEntity<List<Review>> getReviewsByUserId(
            @Parameter(description = "ID of the user") @PathVariable Long userId) {
        logger.debug("Request to get reviews for user ID: {}", userId);
        List<Review> reviews = reviewService.findByUserId(userId);

        if (reviews.isEmpty()) {
            logger.debug("Reviews for user ID {} not found (returning 204)", userId);
            return ResponseEntity.noContent().build();
        }

        logger.debug("Found {} reviews for user ID {}", reviews.size(), userId);
        return ResponseEntity.ok(reviews);
    }

    @PostMapping
    @Operation(summary = "Создать новый отзыв",
            description = "Создает новый отзыв на основе предоставленных данных")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201",
                    description = "Отзыв успешно создан",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = Review.class))),
            @ApiResponse(responseCode = "400",
                    description = "Неверные входные данные", content = @Content),
            @ApiResponse(responseCode = "404",
                    description = "Фильм или пользователь не найдены", content = @Content)
    })
    // @Transactional // Transactionality should be handled by the service layer
    public ResponseEntity<Review> createReview(@Valid @RequestBody ReviewRequest reviewRequest) {
        logger.debug("Запрос на создание нового отзыва: {}", reviewRequest);

        try {
            Review createdReview = reviewService.createReview(reviewRequest);
            logger.info("Отзыв с ID '{}' успешно создан.", createdReview.getId());
            return ResponseEntity.status(HttpStatus.CREATED).body(createdReview);

        } catch (RuntimeException e) {
            logger.error("Ошибка при создании отзыва: {}", e.getMessage());
            if (e.getMessage() != null && (e.getMessage().contains("Фильм не найден") || e.getMessage().contains("Пользователь не найден"))) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        } catch (Exception e) {
            logger.error("Непредвиденная ошибка при создании отзыва:", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }


    @PutMapping("/{id}")
    @Operation(summary = "Обновить отзыв",
            description = "Обновляет существующий отзыв с указанным ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Отзыв успешно обновлен",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = Review.class))),
            @ApiResponse(responseCode = "400",
                    description = "Неверные входные данные", content = @Content),
            @ApiResponse(responseCode = "404", description = "Отзыв, фильм или пользователь не найдены", content = @Content)
    })
    // @Transactional // Transactionality should be handled by the service layer
    public ResponseEntity<Review> updateReview(
            @Parameter(description = "Идентификатор отзыва для обновления",
                    example = "1") @PathVariable Long id,
            @Valid @RequestBody ReviewRequest reviewRequest) {
        logger.debug("Запрос на обновление отзыва с ID: {}", id);
        try {
            Review updatedReview = reviewService.updateReview(id, reviewRequest);
            logger.info("Отзыв с ID '{}' успешно обновлен.", updatedReview.getId());
            return ResponseEntity.ok(updatedReview);

        } catch (RuntimeException e) {
            logger.error("Ошибка при обновлении отзыва с ID {}: {}", id, e.getMessage());
            if (e.getMessage() != null && (e.getMessage().contains("Отзыв не найден") || e.getMessage().contains("Фильм не найден") || e.getMessage().contains("Пользователь не найден"))) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        } catch (Exception e) {
            logger.error("Непредвиденная ошибка при обновлении отзыва с ID {}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }


    @DeleteMapping("/{id}")
    @Operation(summary = "Удалить отзыв", description = "Удаляет отзыв с указанным ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204",
                    description = "Отзыв успешно удален", content = @Content),
            @ApiResponse(responseCode = "404",
                    description = "Отзыв не найден", content = @Content)
    })
    // @Transactional // Transactionality should be handled by the service layer
    public ResponseEntity<Void> deleteReview(
            @Parameter(description = "Идентификатор отзыва для удаления",
                    example = "1") @PathVariable Long id) {
        logger.debug("Запрос на удаление отзыва с ID: {}", id);
        try {
            reviewService.deleteById(id);
            logger.info("Отзыв с ID: {} успешно удален.", id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            logger.error("Ошибка при удалении отзыва с ID {}: {}", id, e.getMessage());
            if (e.getMessage() != null && e.getMessage().contains("Отзыв не найден")) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        } catch (Exception e) {
            logger.error("Непредвиденная ошибка при удалении отзыва с ID {}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
