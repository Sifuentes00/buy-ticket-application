package com.matvey.cinema.controllers;

import com.matvey.cinema.model.entities.Favorite;
import com.matvey.cinema.model.entities.Movie;
import com.matvey.cinema.repository.FavoriteRepository;
import com.matvey.cinema.repository.MovieRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/favorites")
public class FavoriteController {

    @Autowired
    private FavoriteRepository favoriteRepository;

    @Autowired
    private MovieRepository movieRepository;

    @GetMapping
    public ResponseEntity<List<Movie>> getUserFavorites(@RequestParam String userId) {
        List<Movie> favoriteMovies = favoriteRepository.findAllByKeycloakUserId(userId)
                .stream()
                .map(Favorite::getMovie)
                .collect(Collectors.toList());
        return ResponseEntity.ok(favoriteMovies);
    }

    @PostMapping
    public ResponseEntity<?> addFavorite(@RequestParam String userId, @RequestParam Long movieId) {
        if (favoriteRepository.existsByKeycloakUserIdAndMovieId(userId, movieId)) {
            return ResponseEntity.badRequest().body("Фильм уже в избранном");
        }
        Movie movie = movieRepository.findById(movieId)
                .orElseThrow(() -> new RuntimeException("Фильм не найден"));

        Favorite favorite = new Favorite(userId, movie);
        favoriteRepository.save(favorite);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping
    @Transactional
    public ResponseEntity<?> removeFavorite(@RequestParam String userId, @RequestParam Long movieId) {
        favoriteRepository.deleteByKeycloakUserIdAndMovieId(userId, movieId);
        return ResponseEntity.ok().build();
    }
}