package com.matvey.cinema.model.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.matvey.cinema.validation.ValidShowtimeDate;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ShowtimeRequest {

    @NotBlank(message = "Поле 'dateTime' не должно быть пустым")
    private String dateTime;

    @NotBlank(message = "Поле 'type' не должно быть пустым")
    private String type;

    @NotNull(message = "Поле 'movieId' не должно быть пустым")
    private Long movieId;

    @NotNull(message = "Поле 'theaterId' не должно быть пустым")
    private Long theaterId;

    private List<Long> ticketIds;

    public String getDateTime() {
        return dateTime;
    }

    public String getType() {
        return type;
    }

    public Long getMovieId() {
        return movieId;
    }

    public Long getTheaterId() {
        return theaterId;
    }

    public List<Long> getTicketIds() {
        return ticketIds;
    }
}
