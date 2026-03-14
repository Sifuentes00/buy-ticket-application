package com.matvey.cinema.controllers;

import com.matvey.cinema.model.dto.ReviewRequest;
import com.matvey.cinema.model.entities.Review;
import com.matvey.cinema.service.ReviewService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import jakarta.validation.Valid;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/reviews")
@Tag(name = "Review Controller", description = "API для управления отзывами")
public class ReviewController {

    private static final Logger logger = LoggerFactory.getLogger(ReviewController.class);

    private final ReviewService reviewService;

    public ReviewController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    @GetMapping
    @Operation(summary = "Получить все отзывы")
    public ResponseEntity<List<Review>> getAllReviews() {

        logger.info("event=api_get_all_reviews");

        List<Review> reviews = reviewService.findAll();

        return ResponseEntity.ok(reviews);
    }

    @GetMapping("/movie/{movieId}")
    @Operation(summary = "Получить отзывы по фильму")
    public ResponseEntity<List<Review>> getReviewsByMovieId(@PathVariable Long movieId) {

        logger.info("event=api_get_reviews_by_movie movieId={}", movieId);

        List<Review> reviews = reviewService.findReviewsByMovieId(movieId);

        if (reviews.isEmpty()) {
            return ResponseEntity.noContent().build();
        }

        return ResponseEntity.ok(reviews);
    }

    @GetMapping("/my")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<Review>> getMyReviews() {

        logger.info("event=api_get_my_reviews");

        return ResponseEntity.ok(reviewService.findMyReviews());
    }

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Review> createReview(
            @Valid @RequestBody ReviewRequest reviewRequest,
            @AuthenticationPrincipal Jwt jwt) {

        String userId = jwt.getSubject();

        logger.info(
                "event=api_create_review movieId={} userId={}",
                reviewRequest.getMovieId(),
                userId
        );

        String username = jwt.getClaim("preferred_username");
        String email = jwt.getClaim("email");

        Review createdReview = reviewService.createReview(
                reviewRequest,
                userId,
                username,
                email
        );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(createdReview);
    }

    @PutMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Review> updateReview(
            @PathVariable Long id,
            @Valid @RequestBody ReviewRequest reviewRequest) {

        logger.info("event=api_update_review reviewId={}", id);

        try {

            Review updatedReview = reviewService.updateReview(id, reviewRequest);

            return ResponseEntity.ok(updatedReview);

        } catch (RuntimeException e) {

            logger.error(
                    "event=api_update_review_error reviewId={} message={}",
                    id,
                    e.getMessage()
            );

            if (e.getMessage() != null && e.getMessage().contains("Отзыв не найден")) {
                return ResponseEntity.notFound().build();
            }

            return ResponseEntity.badRequest().build();

        } catch (Exception e) {

            logger.error(
                    "event=api_update_review_unexpected reviewId={}",
                    id,
                    e
            );

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> deleteReview(@PathVariable Long id) {

        logger.info("event=api_delete_review reviewId={}", id);

        try {

            reviewService.deleteById(id);

            return ResponseEntity.noContent().build();

        } catch (RuntimeException e) {

            logger.error(
                    "event=api_delete_review_error reviewId={} message={}",
                    id,
                    e.getMessage()
            );

            if (e.getMessage() != null && e.getMessage().contains("Отзыв не найден")) {
                return ResponseEntity.notFound().build();
            }

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();

        } catch (Exception e) {

            logger.error(
                    "event=api_delete_review_unexpected reviewId={}",
                    id,
                    e
            );

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}