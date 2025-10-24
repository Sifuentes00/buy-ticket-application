package com.matvey.cinema.model.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = false)
public class TheaterRequest {

    @NotBlank(message = "Поле 'name' не должно быть пустым")
    private String name;

    @NotNull(message = "Поле 'capacity' не должно быть пустым")
    @Positive(message = "Поле 'capacity' должно быть положительным числом")
    private Integer capacity; // Изменено на Integer, чтобы использовать @NotNull

    private List<Long> seatIds;
    private List<Long> showtimeIds;

    public String getName() {
        return name;
    }

    public Integer getCapacity() {
        return capacity;
    }

    public List<Long> getSeatIds() {
        return seatIds;
    }

    public List<Long> getShowtimeIds() {
        return showtimeIds;
    }
}
