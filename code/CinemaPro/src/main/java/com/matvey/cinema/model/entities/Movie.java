// In file com.matvey.cinema.model.entities.Movie.java

package com.matvey.cinema.model.entities;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "movies")
public class Movie {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Название фильма не должно быть пустым")
    private String title;

    @NotBlank(message = "Режиссер не должен быть пустым")
    private String director;

    @Min(value = 1800, message = "Год выхода должен быть не ранее 1800")
    @NotNull(message = "Год выхода не должен быть пустым")
    private Integer releaseYear;

    @NotBlank(message = "Жанр не должен быть пустым")
    private String genre;

    @OneToMany(mappedBy = "movie", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private List<Showtime> showtimes = new ArrayList<>();

    @OneToMany(mappedBy = "movie", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    private List<Review> reviews = new ArrayList<>();

    public Movie() {
        // Default constructor
    }

    // Getters and Setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDirector() {
        return director;
    }

    public void setDirector(String director) {
        this.director = director;
    }

    public Integer getReleaseYear() {
        return releaseYear;
    }

    public void setReleaseYear(Integer releaseYear) {
        this.releaseYear = releaseYear;
    }

    public String getGenre() {
        return genre;
    }

    public void setGenre(String genre) {
        this.genre = genre;
    }

    public List<Showtime> getShowtimes() {
        return showtimes;
    }

    public void setShowtimes(List<Showtime> showtimes) {
        this.showtimes = showtimes;
    }

    public List<Review> getReviews() {
        return reviews;
    }

    public void setReviews(List<Review> reviews) {
        this.reviews = reviews;
    }

    public void addReview(Review review) {
        reviews.add(review);
        review.setMovie(this);
    }

    public void removeReview(Review review) {
        reviews.remove(review);
        review.setMovie(null);
    }
}
