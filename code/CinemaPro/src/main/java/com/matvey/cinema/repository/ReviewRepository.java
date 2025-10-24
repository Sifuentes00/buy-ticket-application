package com.matvey.cinema.repository;

import java.util.List;

import com.matvey.cinema.model.entities.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {

    @Query("SELECT r FROM Review r WHERE r.content = :content")
    List<Review> findReviewsByContent(@Param("content") String content);

    @Query(value =
            "SELECT r.* FROM reviews r JOIN movies m ON r.movie_id = m.id" + " WHERE m.title = ?1",
            nativeQuery = true)
    List<Review> findReviewsByMovieTitle(String movieTitle);

    @Query(value =
            "SELECT r.* FROM reviews r JOIN users u ON r.user_id = u.id" + " WHERE u.username = ?1",
            nativeQuery = true)
    List<Review> findReviewsByUserUsername(String username);

    @Query(value = "SELECT * FROM reviews r WHERE r.movie_id = :movieId", nativeQuery = true)
    List<Review> findReviewsByMovieIdNative(@Param("movieId") Long movieId);

    List<Review> findByUserId(Long userId);

    // TODO: Remove or refactor this default method if it's not used or causing issues
    // default void updateReviewDetails(...) { ... }
}
