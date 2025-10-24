package com.matvey.cinema.service.impl;

import com.matvey.cinema.cache.CacheKeys;
import com.matvey.cinema.cache.InMemoryCache;
import com.matvey.cinema.exception.CustomNotFoundException;
import com.matvey.cinema.model.entities.Review;
import com.matvey.cinema.repository.ReviewRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReviewServiceImplTest {

    @Mock
    private ReviewRepository reviewRepository;

    @Mock
    private InMemoryCache cache;

    @InjectMocks
    private ReviewServiceImpl reviewService;

    private Review review;


    @Test
    void testFindById_ReviewFoundInCache() {
        String cacheKey = CacheKeys.REVIEW_PREFIX + review.getId();
        when(cache.get(cacheKey)).thenReturn(Optional.of(review));

        Optional<Review> foundReview = reviewService.findById(review.getId());

        assertTrue(foundReview.isPresent());
        assertEquals(review, foundReview.get());
        verify(cache, times(1)).get(cacheKey);
        verify(reviewRepository, never()).findById(anyLong());
    }

    @Test
    void testFindById_ReviewFoundInRepository() {
        when(cache.get(CacheKeys.REVIEW_PREFIX + review.getId())).thenReturn(Optional.empty());
        when(reviewRepository.findById(review.getId())).thenReturn(Optional.of(review));

        Optional<Review> foundReview = reviewService.findById(review.getId());

        assertTrue(foundReview.isPresent());
        assertEquals(review, foundReview.get());
        verify(reviewRepository, times(1)).findById(review.getId());
        verify(cache, times(1)).put(CacheKeys.REVIEW_PREFIX + review.getId(), review);
    }

    @Test
    void testFindById_ReviewNotFound() {
        when(cache.get(CacheKeys.REVIEW_PREFIX + review.getId())).thenReturn(Optional.empty());
        when(reviewRepository.findById(review.getId())).thenReturn(Optional.empty());

        Long reviewId = review.getId();
        assertThrows(CustomNotFoundException.class, () -> reviewService.findById(reviewId));
        verify(reviewRepository, times(1)).findById(review.getId());
    }

    @Test
    void testFindAll_ReviewsFoundInCache() {
        String cacheKey = CacheKeys.REVIEWS_ALL;
        when(cache.get(cacheKey)).thenReturn(Optional.of(Collections.singletonList(review)));

        List<Review> reviews = reviewService.findAll();

        assertEquals(1, reviews.size());
        assertEquals(review, reviews.get(0));
        verify(cache, times(1)).get(cacheKey);
        verify(reviewRepository, never()).findAll();
    }

    @Test
    void testFindAll_ReviewsNotFoundInCache() {
        String cacheKey = CacheKeys.REVIEWS_ALL;
        when(cache.get(cacheKey)).thenReturn(Optional.empty());
        when(reviewRepository.findAll()).thenReturn(Collections.singletonList(review));

        List<Review> reviews = reviewService.findAll();

        assertEquals(1, reviews.size());
        assertEquals(review, reviews.get(0));
        verify(reviewRepository, times(1)).findAll();
        verify(cache, times(1)).put(cacheKey, Collections.singletonList(review));
    }


    @Test
    void testFindReviewsByMovieTitle_ReviewsFoundInCache() {
        String movieTitle = "Inception";
        String cacheKey = CacheKeys.REVIEWS_MOVIE_PREFIX + movieTitle;
        when(cache.get(cacheKey)).thenReturn(Optional.of(Collections.singletonList(review)));

        List<Review> reviews = reviewService.findReviewsByMovieTitle(movieTitle);

        assertEquals(1, reviews.size());
        assertEquals(review, reviews.get(0));
        verify(cache, times(1)).get(cacheKey);
        verify(reviewRepository, never()).findReviewsByMovieTitle(anyString());
    }

    @Test
    void testFindReviewsByMovieTitle_ReviewsNotFoundInCache() {
        String movieTitle = "Inception";
        String cacheKey = CacheKeys.REVIEWS_MOVIE_PREFIX + movieTitle;
        when(cache.get(cacheKey)).thenReturn(Optional.empty());
        when(reviewRepository.findReviewsByMovieTitle(movieTitle)).thenReturn(Collections.singletonList(review));

        List<Review> reviews = reviewService.findReviewsByMovieTitle(movieTitle);

        assertEquals(1, reviews.size());
        assertEquals(review, reviews.get(0));
        verify(reviewRepository, times(1)).findReviewsByMovieTitle(movieTitle);
        verify(cache, times(1)).put(cacheKey, Collections.singletonList(review));
    }

    @Test
    void testFindReviewsByUserUsername_ReviewsFoundInCache() {
        String username = "TestUser";
        String cacheKey = CacheKeys.REVIEWS_USER_PREFIX + username;
        when(cache.get(cacheKey)).thenReturn(Optional.of(Collections.singletonList(review)));

        List<Review> reviews = reviewService.findReviewsByUserUsername(username);

        assertEquals(1, reviews.size());
        assertEquals(review, reviews.get(0));
        verify(cache, times(1)).get(cacheKey);
        verify(reviewRepository, never()).findReviewsByUserUsername(anyString());
    }

    @Test
    void testFindReviewsByUserUsername_ReviewsNotFoundInCache() {
        String username = "TestUser";
        String cacheKey = CacheKeys.REVIEWS_USER_PREFIX + username;
        when(cache.get(cacheKey)).thenReturn(Optional.empty());
        when(reviewRepository.findReviewsByUserUsername(username)).thenReturn(Collections.singletonList(review));

        List<Review> reviews = reviewService.findReviewsByUserUsername(username);

        assertEquals(1, reviews.size());
        assertEquals(review, reviews.get(0));
        verify(reviewRepository, times(1)).findReviewsByUserUsername(username);
        verify(cache, times(1)).put(cacheKey, Collections.singletonList(review));
    }

    @Test
    void testSave_ReviewSuccessfullySaved() {
        when(reviewRepository.save(review)).thenReturn(review);

        Review savedReview = reviewService.save(review);

        assertEquals(review, savedReview);
        verify(reviewRepository, times(1)).save(review);
        verify(cache, times(1)).evict(CacheKeys.REVIEWS_ALL);
        verify(cache, times(1)).evict(CacheKeys.REVIEW_PREFIX + savedReview.getId());
        verify(cache, times(1)).evict(CacheKeys.REVIEWS_CONTENT_PREFIX + savedReview.getContent());
    }
}


