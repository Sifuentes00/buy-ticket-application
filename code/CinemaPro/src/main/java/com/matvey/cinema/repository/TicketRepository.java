package com.matvey.cinema.repository;

import com.matvey.cinema.model.entities.Seat;
import com.matvey.cinema.model.entities.Showtime;
import com.matvey.cinema.model.entities.Ticket;
// Import entities if needed for methods below, or if native queries reference them
// import com.matvey.cinema.model.entities.User;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param; // Needed for @Param
import org.springframework.stereotype.Repository;

@Repository
public interface TicketRepository extends JpaRepository<Ticket, Long> {

    // Your original methods (assuming they work with your DB schema)
    @Query(value =
            "SELECT t.* FROM tickets t JOIN users u ON t.user_id = u.id WHERE u.username = ?1",
            nativeQuery = true)
    List<Ticket> findTicketsByUserUsername(String username);

    @Query(value =
            "SELECT t.* FROM tickets t JOIN showtimes s ON t.showtime_id=s.id WHERE s.date_time=?1",
            nativeQuery = true)
    List<Ticket> findTicketsByShowtimeDateTime(String showtimeDateTime);

    @Query(value = "SELECT * FROM tickets WHERE seat_id = ?1", nativeQuery = true)
    List<Ticket> findTicketsBySeatId(Long seatId);

    // <-- RETURNED: Your original methods for finding foreign key IDs -->
    @Query(value = "SELECT user_id FROM tickets WHERE id = :id", nativeQuery = true)
    Optional<Long> findUserIdById(@Param("id") Long id);

    @Query(value = "SELECT showtime_id FROM tickets WHERE id = :id", nativeQuery = true)
    Optional<Long> findShowtimeIdById(@Param("id") Long id);

    @Query(value = "SELECT seat_id FROM tickets WHERE id = :id", nativeQuery = true)
    Optional<Long> findSeatIdById(@Param("id") Long id);

    List<Ticket> findByUser_Username(String username);

    // If Ticket entity has ManyToOne Showtime showtime:
    List<Ticket> findByShowtime_DateTime(String showtimeDateTime); // If datetime is String
    List<Ticket> findByShowtime(Showtime showtime); // Find by Showtime entity

    // If Ticket entity has ManyToOne Seat seat:
    List<Ticket> findBySeatId(Long seatId); // Find by Seat entity ID using Spring Data

    // Method for finding a ticket by Showtime and Seat number (for purchase validation)
    Optional<Ticket> findByShowtimeAndSeatNumber(Showtime showtime, String seatNumber);

    // Your original method to find by Showtime ID
    List<Ticket> findByShowtime_Id(Long showtimeId); // Keeping your method

    @EntityGraph(attributePaths = {"showtime", "user", "seat", "showtime.movie", "showtime.theater"})
    List<Ticket> findByUserId(Long userId);
}