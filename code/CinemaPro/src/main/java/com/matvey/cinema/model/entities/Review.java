// In file com.matvey.cinema.model.entities.Review.java

package com.matvey.cinema.model.entities;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
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

    @NotNull(message = "Отзыв должен быть связан с пользователем")
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

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

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Movie getMovie() {
        return movie;
    }

    public void setMovie(Movie movie) {
        this.movie = movie;
    }
}
