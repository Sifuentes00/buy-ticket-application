package com.matvey.cinema.model.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Min; // Возможно, понадобятся для валидации
import jakarta.validation.constraints.Max; // Возможно, понадобятся для валидации

@JsonIgnoreProperties(ignoreUnknown = true)
public class ReviewRequest {

    @NotBlank(message = "Поле 'content' не должно быть пустым")
    private String content;

    @NotNull(message = "Поле 'movieId' не должно быть пустым")
    private Long movieId;

    @NotNull(message = "Поле 'userId' не должно быть пустым")
    private Long userId;

    @NotNull(message = "Поле 'rating' не должно быть пустым") // Добавьте, если рейтинг обязателен
    @Min(value = 1, message = "Рейтинг должен быть не меньше 1") // Добавьте, если есть мин. значение
    @Max(value = 10, message = "Рейтинг должен быть не больше 10") // Добавьте, если есть макс. значение
    private Integer rating; // Используем Integer для @NotNull и возможности null

    public String getContent() {
        return content;
    }

    public Long getMovieId() {
        return movieId;
    }

    public Long getUserId() {
        return userId;
    }


    public Integer getRating() {
        return rating;
    }

    public void setRating(Integer rating) {
        this.rating = rating;
    }
}