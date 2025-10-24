package com.matvey.cinema.service;

import com.matvey.cinema.model.entities.Seat;
import java.util.List;
import java.util.Optional;

public interface SeatService {
    Optional<Seat> findById(Long id);

    List<Seat> findAll();

    List<Seat> findSeatsByTheaterName(String theaterName);

    Seat save(Seat seat);

    void deleteById(Long id);
}