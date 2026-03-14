package com.matvey.cinema.service.impl;

import com.matvey.cinema.cache.InMemoryCache;
import com.matvey.cinema.model.dto.ReviewRequest;
import com.matvey.cinema.model.entities.Movie;
import com.matvey.cinema.model.entities.Review;
import com.matvey.cinema.repository.MovieRepository;
import com.matvey.cinema.repository.ReviewRepository;
import com.matvey.cinema.service.MovieService;
import com.matvey.cinema.service.ReviewService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;
import java.util.Optional;

@Service
public class ReviewServiceImpl implements ReviewService {

    private static final Logger logger = LoggerFactory.getLogger(ReviewServiceImpl.class);

    private final ReviewRepository reviewRepository;
    private final MovieRepository movieRepository;
    private final InMemoryCache cache;
    private final MovieService movieService;

    @Autowired
    public ReviewServiceImpl(
            ReviewRepository reviewRepository,
            MovieRepository movieRepository,
            InMemoryCache cache,
            MovieService movieService
    ) {
        this.reviewRepository = reviewRepository;
        this.movieRepository = movieRepository;
        this.cache = cache;
        this.movieService = movieService;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Review> findById(Long id) {

        String cacheKey = "review::id:" + id;

        logger.info("event=review_find_by_id reviewId={}", id);

        Optional<Object> cachedData = cache.get(cacheKey);

        if (cachedData.isPresent()) {

            logger.debug("event=cache_hit cacheKey={}", cacheKey);

            Object data = cachedData.get();

            if (data instanceof Review) {
                return Optional.of((Review) data);
            }

            cache.evict(cacheKey);

            logger.warn("event=cache_invalid_type cacheKey={}", cacheKey);
        }

        Optional<Review> review = reviewRepository.findById(id);

        if (review.isEmpty()) {
            logger.warn("event=review_not_found reviewId={}", id);
            return Optional.empty();
        }

        review.ifPresent(value -> {
            cache.put(cacheKey, value);
            logger.debug("event=cache_put cacheKey={} reviewId={}", cacheKey, id);
        });

        return review;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Review> findAll() {

        String cacheKey = "review::all";

        logger.info("event=review_find_all");

        Optional<Object> cachedData = cache.get(cacheKey);

        if (cachedData.isPresent()) {

            logger.debug("event=cache_hit cacheKey={}", cacheKey);

            Object data = cachedData.get();

            if (data instanceof List) {
                List<?> list = (List<?>) data;

                if (list.isEmpty() || list.get(0) instanceof Review) {
                    return (List<Review>) data;
                }
            }

            cache.evict(cacheKey);

            logger.warn("event=cache_invalid_type cacheKey={}", cacheKey);
        }

        List<Review> reviews = reviewRepository.findAll();

        cache.put(cacheKey, reviews);

        logger.debug("event=cache_put cacheKey={} size={}", cacheKey, reviews.size());

        return reviews;
    }

    @Override
    @Transactional
    public Review save(Review review) {

        logger.info("event=review_save_start reviewId={}", review.getId());

        Review savedReview = reviewRepository.save(review);

        logger.info("event=review_saved reviewId={}", savedReview.getId());

        cache.evict("review::all");

        if (savedReview.getId() != null) {
            cache.evict("review::id:" + savedReview.getId());
        }

        Optional.ofNullable(savedReview.getMovie())
                .map(Movie::getId)
                .ifPresent(movieId -> {

                    cache.evict("review::movie_id:" + movieId);

                    movieService.evictMovieCache(movieId);
                    movieService.evictAllMoviesWithReviewsCache();

                    logger.debug(
                            "event=review_cache_evicted movieId={} reviewId={}",
                            movieId,
                            savedReview.getId()
                    );
                });

        return savedReview;
    }

    @Override
    @Transactional
    public Review createReview(
            ReviewRequest reviewRequest,
            String keycloakUserId,
            String username,
            String email
    ) {

        logger.info(
                "event=review_create_start movieId={} userId={}",
                reviewRequest.getMovieId(),
                keycloakUserId
        );

        Movie movie = movieRepository
                .findById(reviewRequest.getMovieId())
                .orElseThrow(() -> {
                    logger.error("event=movie_not_found movieId={}", reviewRequest.getMovieId());
                    return new RuntimeException("Фильм не найден");
                });

        Review review = new Review();

        review.setContent(reviewRequest.getContent());
        review.setRating(reviewRequest.getRating());
        review.setMovie(movie);

        review.setKeycloakUserId(keycloakUserId);
        review.setUsername(username);
        review.setEmail(email);

        return save(review);
    }

    @Override
    @Transactional
    public Review updateReview(Long reviewId, ReviewRequest reviewRequest) {

        logger.info("event=review_update_start reviewId={}", reviewId);

        Review existingReview = reviewRepository
                .findById(reviewId)
                .orElseThrow(() -> {
                    logger.error("event=review_not_found reviewId={}", reviewId);
                    return new RuntimeException("Отзыв не найден с ID: " + reviewId);
                });

        if (reviewRequest.getRating() != null) {
            existingReview.setRating(reviewRequest.getRating());
        }

        if (reviewRequest.getContent() != null) {
            existingReview.setContent(reviewRequest.getContent());
        }

        Review savedReview = save(existingReview);

        logger.info("event=review_updated reviewId={}", savedReview.getId());

        return savedReview;
    }

    @Override
    @Transactional
    public void deleteById(Long id) {

        logger.info("event=review_delete_start reviewId={}", id);

        Optional<Review> reviewOpt = reviewRepository.findById(id);

        if (reviewOpt.isEmpty()) {
            logger.warn("event=review_delete_not_found reviewId={}", id);
            throw new RuntimeException("Отзыв не найден с ID: " + id);
        }

        Review review = reviewOpt.get();

        Long movieId = review.getMovie() != null
                ? review.getMovie().getId()
                : null;

        cache.evict("review::all");
        cache.evict("review::id:" + review.getId());

        if (movieId != null) {

            cache.evict("review::movie_id:" + movieId);

            movieService.evictMovieCache(movieId);
            movieService.evictAllMoviesWithReviewsCache();

            logger.debug("event=review_delete_cache_evicted movieId={}", movieId);
        }

        reviewRepository.deleteById(id);

        logger.info("event=review_deleted reviewId={}", id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Review> findReviewsByMovieTitle(String movieTitle) {

        String cacheKey = "review::movie_title:" + movieTitle;

        logger.info("event=review_find_by_movie_title movieTitle={}", movieTitle);

        Optional<Object> cachedData = cache.get(cacheKey);

        if (cachedData.isPresent()) {

            logger.debug("event=cache_hit cacheKey={}", cacheKey);

            Object data = cachedData.get();

            if (data instanceof List) {
                List<?> list = (List<?>) data;

                if (list.isEmpty() || list.get(0) instanceof Review) {
                    return (List<Review>) data;
                }
            }

            cache.evict(cacheKey);

            logger.warn("event=cache_invalid_type cacheKey={}", cacheKey);
        }

        List<Review> reviews = reviewRepository.findReviewsByMovieTitle(movieTitle);

        cache.put(cacheKey, reviews);

        logger.debug("event=cache_put cacheKey={} size={}", cacheKey, reviews.size());

        return reviews;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Review> findReviewsByMovieId(Long movieId) {

        String cacheKey = "review::movie_id:" + movieId;

        logger.info("event=review_find_by_movie_id movieId={}", movieId);

        Optional<Object> cachedData = cache.get(cacheKey);

        if (cachedData.isPresent()) {

            logger.debug("event=cache_hit cacheKey={}", cacheKey);

            Object data = cachedData.get();

            if (data instanceof List) {
                List<?> list = (List<?>) data;

                if (list.isEmpty() || list.get(0) instanceof Review) {
                    return (List<Review>) data;
                }
            }

            cache.evict(cacheKey);

            logger.warn("event=cache_invalid_type cacheKey={}", cacheKey);
        }

        List<Review> reviews = reviewRepository.findReviewsByMovieIdNative(movieId);

        cache.put(cacheKey, reviews);

        logger.debug("event=cache_put cacheKey={} size={}", cacheKey, reviews.size());

        return reviews;
    }

    @Transactional(readOnly = true)
    public List<Review> findMyReviews() {

        var authentication = SecurityContextHolder.getContext().getAuthentication();
        var jwt = (org.springframework.security.oauth2.jwt.Jwt) authentication.getPrincipal();

        String keycloakUserId = jwt.getSubject();

        logger.info("event=review_find_my_reviews userId={}", keycloakUserId);

        return reviewRepository.findByKeycloakUserId(keycloakUserId);
    }
}