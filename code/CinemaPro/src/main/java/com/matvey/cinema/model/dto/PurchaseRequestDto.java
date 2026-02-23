package com.matvey.cinema.model.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;

// <-- ДОБАВЛЕНО: Новый DTO для покупки -->
public class PurchaseRequestDto {

    @NotNull(message = "Поле 'showtimeId' не должно быть пустым")
    private Long showtimeId;

    @NotNull(message = "Список номеров мест не должен быть пустым")
    @NotEmpty // Список номеров мест не должен быть пустым
    private List<String> seatNumbers; // Список номеров мест, как на фронтенде ("1-5")

    public PurchaseRequestDto() {
    }

    public Long getShowtimeId() {
        return showtimeId;
    }

    public void setShowtimeId(Long showtimeId) {
        this.showtimeId = showtimeId;
    }

    public List<String> getSeatNumbers() {
        return seatNumbers;
    }

    public void setSeatNumbers(List<String> seatNumbers) {
        this.seatNumbers = seatNumbers;
    }

}