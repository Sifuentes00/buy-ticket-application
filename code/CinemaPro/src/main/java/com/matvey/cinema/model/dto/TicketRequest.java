package com.matvey.cinema.model.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.NotNull;
// Removed @Positive because price can be null
// import jakarta.validation.constraints.Positive;

@JsonIgnoreProperties(ignoreUnknown = false)
public class TicketRequest {

    // Changed primitive double to wrapper class Double to allow null
    // If price is optional, remove @Positive or handle null case before validation
    // @Positive(message = "Поле 'price' должно быть положительным числом")
    private Double price; // <-- ИЗМЕНЕНО: Double вместо double

    @NotNull(message = "Поле 'showtimeId' не должно быть пустым")
    private Long showtimeId;

    @NotNull(message = "Поле 'userId' не должно быть пустым")
    private Long userId;

    @NotNull(message = "Поле 'seatId' не должно быть пустым")
    private Long seatId;

    public TicketRequest() {
    }

    // Getters and Setters
    public Double getPrice() { // <-- ИЗМЕНЕНО: Возвращает Double
        return price;
    }

    public void setPrice(Double price) { // <-- ИЗМЕНЕНО: Принимает Double
        this.price = price;
    }

    public Long getShowtimeId() {
        return showtimeId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getSeatId() {
        return seatId;
    }

    public void setSeatId(Long seatId) {
        this.seatId = seatId;
    }
}