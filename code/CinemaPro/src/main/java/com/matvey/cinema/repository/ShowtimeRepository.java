package com.matvey.cinema.repository;

import com.matvey.cinema.model.dto.ShowtimeRequest;
import com.matvey.cinema.model.entities.Movie;
import com.matvey.cinema.model.entities.Showtime;
import com.matvey.cinema.model.entities.Theater;
import com.matvey.cinema.model.entities.Ticket;
import com.matvey.cinema.service.MovieService;
import com.matvey.cinema.service.TheaterService;
import com.matvey.cinema.service.TicketService;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ShowtimeRepository extends JpaRepository<Showtime, Long> {

    // Поиск сеансов по названию театра
    @Query(value =
            "SELECT st.* FROM showtimes st JOIN theaters t ON st.theater_id=t.id WHERE t.name = ?1",
            nativeQuery = true)
    List<Showtime> findShowtimesByTheaterName(String theaterName);

    // Поиск сеансов по названию фильма
    @Query(value =
            "SELECT st.* FROM showtimes st JOIN movies m ON st.movie_id = m.id WHERE m.title = ?1",
            nativeQuery = true)
    List<Showtime> findShowtimesByMovieTitle(String movieTitle);


    @Query(value = "SELECT theater_id FROM showtimes WHERE id = :id", nativeQuery = true)
    Optional<Long> findTheaterIdById(@Param("id") Long id);

    @Query(value = "SELECT movie_id FROM showtimes WHERE id = :id", nativeQuery = true)
    Optional<Long> findMovieIdById(@Param("id") Long id);

    default void updateShowtimeDetails(Showtime showtime, ShowtimeRequest showtimeRequest,
                                       MovieService movieService, TheaterService theaterService, TicketService ticketService) {
        // 1. Копируем простые поля (datetime, type, hall - если hall добавили в DTO)
        // Валидация @NotBlank/@NotNull в контроллере уже проверила, что эти поля не null/пустые
        showtime.setDateTime(showtimeRequest.getDateTime());
        showtime.setType(showtimeRequest.getType());
        // Если вы добавили hall в ShowtimeRequest DTO, скопируйте его тоже:
        // showtime.setHall(showtimeRequest.getHall());


        // 2. Копируем связь с Фильмом по ID из DTO
        Long movieId = showtimeRequest.getMovieId();
        if (movieId == null) {
            // Хотя @NotNull в DTO должен ловить это раньше,
            // хорошо иметь защиту или выбросить специфичную ошибку
            throw new IllegalArgumentException("Movie ID cannot be null in ShowtimeRequest.");
        }
        // Ищем сущность Movie по ID
        Movie movie = movieService.findById(movieId)
                .orElseThrow(() -> new RuntimeException("Фильм не найден с ID: " + movieId));
        // Устанавливаем найденную сущность Movie на Showtime
        showtime.setMovie(movie);


        // 3. Копируем связь с Театром по ID из DTO
        Long theaterId = showtimeRequest.getTheaterId();
        if (theaterId == null) {
            // Проверка на null, аналогично movieId
            throw new IllegalArgumentException("Theater ID cannot be null in ShowtimeRequest.");
        }
        // Ищем сущность Theater по ID
        Theater theater = theaterService.findById(theaterId)
                .orElseThrow(() -> new RuntimeException("Театр не найден с ID: " + theaterId));
        // Устанавливаем найденную сущность Theater на Showtime
        showtime.setTheater(theater);

    }

    List<Showtime> findByMovieId(Long movieId);
}
