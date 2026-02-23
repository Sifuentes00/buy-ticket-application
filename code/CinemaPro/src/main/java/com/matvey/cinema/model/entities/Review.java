// In file com.matvey.cinema.model.entities.Review.java

package com.matvey.cinema.model.entities;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Entity
@Table(name = "reviews")
public class Review {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Min(value = 1, message = "Рейтинг должен быть не менее 1")
    @Max(value = 10, message = "Рейтинг должен быть не более 10")
    @NotNull(message = "Рейтинг не должен быть пустым")
    private Integer rating;

    @Size(max = 1000, message = "Комментарий не должен превышать 1000 символов")
    private String content;

    @Column(nullable = false)
    private String keycloakUserId;

    @Column(nullable = false)
    private String username;

    @Column(nullable = false)
    private String email;

    @NotNull(message = "Отзыв должен быть связан с фильмом")
    @ManyToOne
    @JoinColumn(name = "movie_id", nullable = false)
    @JsonBackReference // Breaks Movie <-> Review cycle
    private Movie movie;

    public Review() {
        // Default constructor
    }

    // Getters and Setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Integer getRating() {
        return rating;
    }

    public void setRating(Integer rating) {
        this.rating = rating;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getKeycloakUserId() {
        return keycloakUserId;
    }

    public void setKeycloakUserId(String keycloakUserId) {
        this.keycloakUserId = keycloakUserId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Movie getMovie() {
        return movie;
    }

    public void setMovie(Movie movie) {
        this.movie = movie;
    }
}
