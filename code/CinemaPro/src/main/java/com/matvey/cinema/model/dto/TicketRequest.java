package com.matvey.cinema.model.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.NotNull;

@JsonIgnoreProperties(ignoreUnknown = false)
public class TicketRequest {

    private Double price; // <-- ИЗМЕНЕНО: Double вместо double

    @NotNull(message = "Поле 'showtimeId' не должно быть пустым")
    private Long showtimeId;

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

    public Long getSeatId() {
        return seatId;
    }

    public void setSeatId(Long seatId) {
        this.seatId = seatId;
    }
}