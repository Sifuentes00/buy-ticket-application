package com.matvey.cinema.service.impl;

import com.matvey.cinema.cache.InMemoryCache;
import com.matvey.cinema.model.dto.ReviewRequest;
import com.matvey.cinema.model.entities.Movie;
import com.matvey.cinema.model.entities.Review;
import com.matvey.cinema.model.entities.User;
import com.matvey.cinema.repository.ReviewRepository;
import com.matvey.cinema.repository.UserRepository;
import com.matvey.cinema.service.MovieService;
import com.matvey.cinema.service.ReviewService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class ReviewServiceImpl implements ReviewService {
    private static final Logger logger = LoggerFactory.getLogger(ReviewServiceImpl.class);

    private final ReviewRepository reviewRepository;
    private final UserRepository userRepository;
    private final InMemoryCache cache;
    private final MovieService movieService;

    @Autowired
    public ReviewServiceImpl(ReviewRepository reviewRepository,
                             UserRepository userRepository,
                             InMemoryCache cache,
                             MovieService movieService) {
        this.reviewRepository = reviewRepository;
        this.userRepository = userRepository;
        this.cache = cache;
        this.movieService = movieService;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Review> findById(Long id) {
        String cacheKey = "review::id:" + id; // Более конкретный ключ
        logger.info("Finding review by ID: {}", id);

        Optional<Object> cachedData = cache.get(cacheKey);
        if (cachedData.isPresent()) {
            logger.info("Review ID: {} found in cache.", id);
            Object data = cachedData.get();
            if (data instanceof Review) {
                return Optional.of((Review) data);
            } else {
                cache.evict(cacheKey);
                logger.warn("Incorrect data type in cache for key: {}", cacheKey);
            }
        }

        Optional<Review> review = reviewRepository.findById(id);
        if (review.isEmpty()) {
            logger.warn("Review with ID: {} not found.", id);
            return Optional.empty();
        }

        review.ifPresent(value -> {
            cache.put(cacheKey, value);
            logger.info("Review with ID: {} added to cache.", id);
        });

        return review;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Review> findAll() {
        String cacheKey = "review::all";  // Более конкретный ключ
        logger.info("Getting all reviews.");

        Optional<Object> cachedData = cache.get(cacheKey);
        if (cachedData.isPresent()) {
            logger.info("All reviews found in cache.");
            Object data = cachedData.get();
            if (data instanceof List) {
                List<?> list = (List<?>) data;
                if (!list.isEmpty() && list.get(0) instanceof Review) {
                    return (List<Review>) data;
                } else if (list.isEmpty()) {
                    return (List<Review>) data;
                }
            }
            cache.evict(cacheKey);
            logger.warn("Incorrect data type in cache for key: {}", cacheKey);
        }

        List<Review> reviews = reviewRepository.findAll();
        cache.put(cacheKey, reviews);
        logger.info("All reviews added to cache.");

        return reviews;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Review> findByUserId(Long userId) {
        String cacheKey = "review::user_id:" + userId; // Более конкретный ключ
        logger.info("Finding reviews for user ID: {}", userId);

        Optional<Object> cachedData = cache.get(cacheKey);
        if (cachedData.isPresent()) {
            logger.info("Reviews for user ID {} found in cache.", userId);
            Object data = cachedData.get();
            if (data instanceof List) {
                List<?> list = (List<?>) data;
                if (!list.isEmpty() && list.get(0) instanceof Review) {
                    return (List<Review>) data;
                } else if (list.isEmpty()) {
                    return (List<Review>) data;
                }
            }
            cache.evict(cacheKey);
            logger.warn("Incorrect data type in cache for key: {}", cacheKey);
        }

        List<Review> reviews = reviewRepository.findByUserId(userId);

        cache.put(cacheKey, reviews);
        logger.info("Reviews for user ID {} added to cache.", userId);

        return reviews;
    }


    @Transactional
    @Override
    public Review save(Review review) {
        logger.info("Saving review with ID: {}", review.getId());
        Review savedReview = reviewRepository.save(review);
        logger.info("Review successfully saved with ID: {}", savedReview.getId());

        // Очистка кэша отзывов при сохранении
        cache.evict("review::all");
        if (savedReview.getId() != null) {
            cache.evict("review::id:" + savedReview.getId());
        }
        Optional.ofNullable(savedReview.getUser()).map(User::getId).ifPresent(userId -> {
            cache.evict("review::user_id:" + userId);
            logger.info("Cache for reviews of user ID '{}' cleared upon saving.", userId);
        });
        Optional.ofNullable(savedReview.getMovie()).map(Movie::getId).ifPresent(movieId -> {
            cache.evict("review::movie_id:" + movieId);
            logger.info("Cache for reviews of movie ID '{}' cleared upon saving.", movieId);
            // Очистка кэша фильма и всех фильмов с отзывами
            movieService.evictMovieCache(movieId);
            movieService.evictAllMoviesWithReviewsCache();
            logger.info("Movie cache and all movies with reviews cache cleared for movie ID '{}' upon review saving.", movieId);
        });

        return savedReview;
    }


    @Transactional
    @Override
    public Review createReview(ReviewRequest reviewRequest) {
        logger.info("Creating review from ReviewRequest for movie ID: {}, user ID: {}",
                reviewRequest.getMovieId(), reviewRequest.getUserId());

        Movie movie = movieService.findById(reviewRequest.getMovieId())
                .orElseThrow(() -> new RuntimeException("Фильм не найден с ID: " + reviewRequest.getMovieId()));

        User user = userRepository.findById(reviewRequest.getUserId())
                .orElseThrow(() -> new RuntimeException("Пользователь не найден с ID: " + reviewRequest.getUserId()));

        Review newReview = new Review();
        newReview.setMovie(movie);
        newReview.setUser(user);
        newReview.setRating(reviewRequest.getRating());
        newReview.setContent(reviewRequest.getContent());

        Review savedReview = save(newReview);

        logger.info("Review successfully created and saved with ID: {}", savedReview.getId());

        return savedReview;
    }

    @Transactional
    @Override
    public Review updateReview(Long reviewId, ReviewRequest reviewRequest) {
        logger.info("Updating review with ID: {} from ReviewRequest", reviewId);

        Review existingReview = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new RuntimeException("Отзыв не найден с ID: " + reviewId));

        // Update fields from DTO
        if (reviewRequest.getRating() != null) {
            existingReview.setRating(reviewRequest.getRating());
        }
        if (reviewRequest.getContent() != null) {
            existingReview.setContent(reviewRequest.getContent());
        }

        Review savedReview = save(existingReview);

        logger.info("Review successfully updated and saved with ID: {}", savedReview.getId());

        return savedReview;
    }


    @Override
    @Transactional
    public void deleteById(Long id) {
        logger.info("Deleting review with ID: {}", id);
        Optional<Review> reviewOpt = reviewRepository.findById(id);
        if (reviewOpt.isEmpty()) {
            logger.warn("Review with ID: {} not found for deletion.", id);
            throw new RuntimeException("Отзыв не найден с ID: " + id);
        }

        Review review = reviewOpt.get();
        Long movieId = review.getMovie() != null ? review.getMovie().getId() : null;

        // Очистка кэша перед удалением
        cache.evict("review::all");
        if (review.getId() != null) {
            cache.evict("review::id:" + review.getId());
        }

        Optional.ofNullable(review.getUser()).map(User::getId).ifPresent(userId -> {
            cache.evict("review::user_id:" + userId);
            logger.info("Cache for reviews of user ID '{}' cleared upon deletion.", userId);
        });
        // Очистка кеша фильма и всех фильмов с отзывами при удалении
        if (movieId != null) {
            cache.evict("review::movie_id:" + movieId);
            movieService.evictMovieCache(movieId);
            movieService.evictAllMoviesWithReviewsCache();
            logger.info("Movie cache and all movies with reviews cache cleared for movie ID '{}' upon review deletion.", movieId);
        }

        reviewRepository.deleteById(id);
        logger.info("Review with ID: {} successfully deleted from DB.", id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Review> findReviewsByMovieTitle(String movieTitle) {
        String cacheKey = "review::movie_title:" + movieTitle; // Более конкретный ключ
        logger.info("Finding reviews by movie title: {}", movieTitle);

        Optional<Object> cachedData = cache.get(cacheKey);
        if (cachedData.isPresent()) {
            logger.info("Reviews for movie title {} found in cache.", movieTitle);
            Object data = cachedData.get();
            if (data instanceof List) {
                List<?> list = (List<?>) data;
                if (!list.isEmpty() && list.get(0) instanceof Review) {
                    return (List<Review>) data;
                } else if (list.isEmpty()) {
                    return (List<Review>) data;
                }
            }
            cache.evict(cacheKey);
            logger.warn("Incorrect data type in cache for key: {}", cacheKey);
        }

        List<Review> reviews = reviewRepository.findReviewsByMovieTitle(movieTitle);

        cache.put(cacheKey, reviews);
        logger.info("Reviews for movie title {} added to cache.", movieTitle);

        return reviews;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Review> findReviewsByMovieId(Long movieId) {
        String cacheKey = "review::movie_id:" + movieId;  // Более конкретный ключ
        logger.info("Finding reviews by movie ID: {}", movieId);

        Optional<Object> cachedData = cache.get(cacheKey);
        if (cachedData.isPresent()) {
            logger.info("Reviews for movie ID {} found in cache.", movieId);
            Object data = cachedData.get();
            if (data instanceof List) {
                List<?> list = (List<?>) data;
                if (!list.isEmpty() && list.get(0) instanceof Review) {
                    return (List<Review>) data;
                } else if (list.isEmpty()) {
                    return (List<Review>) data;
                }
            }
            cache.evict(cacheKey);
            logger.warn("Incorrect data type in cache for key: {}", cacheKey);
        }

        List<Review> reviews = reviewRepository.findReviewsByMovieIdNative(movieId);

        cache.put(cacheKey, reviews);
        logger.info("Reviews for movie ID {} added to cache.", movieId);

        return reviews;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Review> findReviewsByUserUsername(String userUsername) {
        String cacheKey = "review::user_username:" + userUsername; // Более конкретный ключ
        logger.info("Finding reviews by user username: {}", userUsername);

        Optional<Object> cachedData = cache.get(cacheKey);
        if (cachedData.isPresent()) {
            logger.info("Reviews for user username {} found in cache.", userUsername);
            Object data = cachedData.get();
            if (data instanceof List) {
                List<?> list = (List<?>) data;
                if (!list.isEmpty() && list.get(0) instanceof Review) {
                    return (List<Review>) data;
                } else if (list.isEmpty()) {
                    return (List<Review>) data;
                }
            }
            cache.evict(cacheKey);
            logger.warn("Incorrect data type in cache for key: {}", cacheKey);
        }

        List<Review> reviews = reviewRepository.findReviewsByUserUsername(userUsername);

        cache.put(cacheKey, reviews);
        logger.info("Reviews for user username {} added to cache.", userUsername);

        return reviews;
    }
}

