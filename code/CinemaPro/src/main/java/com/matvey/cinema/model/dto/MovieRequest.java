package com.matvey.cinema.model.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = false)
public class MovieRequest {

    @NotBlank(message = "Поле 'title' не должно быть пустым")
    private String title;

    @NotBlank(message = "Поле 'director' не должно быть пустым")
    private String director;

    @NotNull(message = "Поле 'releaseYear' не должно быть пустым")
    private Integer releaseYear; // Изменено на Integer, чтобы использовать @NotNull

    @NotBlank(message = "Поле 'genre' не должно быть пустым")
    private String genre;

    private List<Long> reviewIds;
    private List<Long> showtimeIds;

    public String getTitle() {
        return title;
    }

    public String getDirector() {
        return director;
    }

    public Integer getReleaseYear() {
        return releaseYear;
    }

    public String getGenre() {
        return genre;
    }

    public List<Long> getReviewIds() {
        return reviewIds;
    }

    public List<Long> getShowtimeIds() {
        return showtimeIds;
    }
}
