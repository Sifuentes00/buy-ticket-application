package com.matvey.cinema.model.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = false)
public class SeatRequest {

    @NotNull(message = "Поле 'seatRow' не должно быть пустым")
    @Positive(message = "Поле 'seatRow' должно быть положительным числом")
    private Integer seatRow; // Изменено на Integer, чтобы использовать @NotNull

    @NotNull(message = "Поле 'number' не должно быть пустым")
    @Positive(message = "Поле 'number' должно быть положительным числом")
    private Integer number; // Изменено на Integer, чтобы использовать @NotNull

    private boolean isAvailable;

    @NotNull(message = "Поле 'theaterId' не должно быть пустым")
    private Long theaterId;

    private List<Long> ticketIds;

    public Integer getSeatRow() {
        return seatRow;
    }

    public Integer getNumber() {
        return number;
    }

    public boolean isAvailable() {
        return isAvailable;
    }

    public Long getTheaterId() {
        return theaterId;
    }

    public List<Long> getTicketIds() {
        return ticketIds;
    }
}
