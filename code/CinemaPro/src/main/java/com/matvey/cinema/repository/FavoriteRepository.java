package com.matvey.cinema.repository;

import com.matvey.cinema.model.entities.Favorite;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FavoriteRepository extends JpaRepository<Favorite, Long> {
    List<Favorite> findAllByKeycloakUserId(String keycloakUserId);
    Optional<Favorite> findByKeycloakUserIdAndMovieId(String keycloakUserId, Long movieId);
    boolean existsByKeycloakUserIdAndMovieId(String keycloakUserId, Long movieId);
    void deleteByKeycloakUserIdAndMovieId(String keycloakUserId, Long movieId);
    void deleteByMovieId(Long movieId);
}