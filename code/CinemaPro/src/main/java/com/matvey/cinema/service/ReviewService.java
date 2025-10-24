package com.matvey.cinema.service;

import com.matvey.cinema.model.dto.ReviewRequest;
import com.matvey.cinema.model.entities.Review;

import java.util.List;
import java.util.Optional;

public interface ReviewService {
    Optional<Review> findById(Long id);

    List<Review> findAll();

    List<Review> findReviewsByMovieTitle(String movieTitle);

    List<Review> findReviewsByMovieId(Long movieId);

    List<Review> findReviewsByUserUsername(String userUsername);

    Review save(Review review);

    void deleteById(Long id);

    List<Review> findByUserId(Long userId);

    Review createReview(ReviewRequest reviewRequest); // Method to create from DTO

    Review updateReview(Long reviewId, ReviewRequest reviewRequest); // Method to update from DTO
}
