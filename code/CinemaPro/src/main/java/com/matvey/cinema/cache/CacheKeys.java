package com.matvey.cinema.cache;

public class CacheKeys {
    public static final String MOVIE_PREFIX = "movie:"; // For caching single movies by ID
    public static final String MOVIES_ALL = "movies:all"; // For caching all movies
    public static final String MOVIES_ALL_WITH_REVIEWS = "movies:all:withReviews";

    public static final String REVIEW_PREFIX = "review_";
    public static final String REVIEWS_ALL = "reviews_all";
    public static final String REVIEWS_CONTENT_PREFIX = "reviews_content_";
    public static final String REVIEWS_MOVIE_PREFIX = "reviews_movie_";
    public static final String REVIEWS_USER_PREFIX = "reviews_user_";

    public static final String REVIEWS_MOVIE_TITLE_PREFIX = "reviews:movie:title:"; // For caching reviews by movie title
    public static final String REVIEWS_USER_USERNAME_PREFIX = "reviews:user:username:";

    public static final String SEAT_PREFIX = "seat_";
    public static final String SEATS_ALL = "seats_all";
    public static final String SEATS_THEATER_PREFIX = "seats_theater_";

    public static final String SHOWTIME_PREFIX = "showtime_";
    public static final String SHOWTIMES_ALL = "showtimes_all";
    public static final String SHOWTIMES_THEATER_PREFIX = "showtimes_theater_";
    public static final String SHOWTIMES_MOVIE_PREFIX = "showtimes_movie_";

    public static final String THEATER_PREFIX = "theater_";
    public static final String THEATERS_ALL = "theaters_all";

    public static final String TICKET_PREFIX = "ticket_";
    public static final String TICKETS_ALL = "tickets_all";
    public static final String TICKETS_USER_PREFIX = "tickets_user_";
    public static final String TICKETS_SHOWTIME_PREFIX = "tickets_showtime_";
    public static final String TICKETS_SEAT_PREFIX = "tickets_seat_";

    public static final String USER_PREFIX = "user_";
    public static final String USERS_ALL = "users_all";

    public static final String TICKETS_BY_ID = "ticketsById";

    public static final String TICKETS_BY_USER = "ticketsByUser";

    public static final String TICKETS_BY_SHOWTIME = "ticketsByShowtime";

    public static final String TICKETS_BY_SEAT = "ticketsBySeat";

    public static final String TICKET_BY_SHOWTIME_AND_SEAT = "ticketByShowtimeAndSeat";

    public static final String TICKET_SHOWTIME_SEAT_PREFIX = "showtimeSeat_";

    private CacheKeys() {
        throw new UnsupportedOperationException("Utility class. Instantiation is not allowed.");
    }
}