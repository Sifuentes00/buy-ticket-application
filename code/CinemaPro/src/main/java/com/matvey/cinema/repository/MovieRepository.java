package com.matvey.cinema.repository;

import com.matvey.cinema.model.entities.Movie;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface MovieRepository extends JpaRepository<Movie, Long> {

    @Query("SELECT m FROM Movie m LEFT JOIN FETCH m.reviews")
    List<Movie> findAllWithReviews();

    // TODO: Add other custom query methods if needed
}
