package com.matvey.cinema.service;

import com.matvey.cinema.model.entities.Theater;
import java.util.List;
import java.util.Optional;

public interface TheaterService {
    Optional<Theater> findById(Long id);

    List<Theater> findAll();

    Theater save(Theater theater);

    void deleteById(Long id);
}
