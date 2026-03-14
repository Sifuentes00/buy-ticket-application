package com.matvey.cinema.controllers;

import com.matvey.cinema.exception.CustomNotFoundException;
import com.matvey.cinema.model.dto.MovieRequest;
import com.matvey.cinema.model.entities.Movie;
import com.matvey.cinema.service.MovieService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/movies")
@Tag(name="Movie Controller",description="API для управления фильмами")
public class MovieController {
    private final MovieService movieService;
    private static final Logger logger=LoggerFactory.getLogger(MovieController.class);
    public MovieController(MovieService movieService){
        this.movieService=movieService;
    }
    @GetMapping("/{id}")
    @Operation(summary="Получить фильм по ID",description="Возвращает фильм с указанным ID")
    @ApiResponses(value={
            @ApiResponse(responseCode="200",description="Фильм успешно получен",content=@Content(mediaType="application/json",schema=@Schema(implementation=Movie.class))),
            @ApiResponse(responseCode="404",description="Фильм не найден",content=@Content)
    })
    public ResponseEntity<Movie> getMovieById(@Parameter(description="Идентификатор фильма",example="1")@PathVariable Long id){
        logger.info("event=api_get_movie_start movieId={}",id);
        Optional<Movie> movie=movieService.findById(id);
        return movie.map(value->{
            logger.info("event=api_get_movie_success movieId={}",id);
            return ResponseEntity.ok(value);
        }).orElseGet(()->{
            logger.warn("event=api_get_movie_not_found movieId={}",id);
            return ResponseEntity.notFound().build();
        });
    }
    @GetMapping
    @Operation(summary="Получить все фильмы",description="Возвращает список всех фильмов")
    public ResponseEntity<List<Movie>> getAllMovies(){
        logger.info("event=api_get_all_movies_start");
        List<Movie> movies=movieService.findAllWithReviews();
        logger.info("event=api_get_all_movies_success count={}",movies.size());
        return ResponseEntity.ok(movies);
    }
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    @Operation(summary="Создать новый фильм",description="Создает новый фильм")
    public ResponseEntity<Movie> createMovie(@Valid@RequestBody MovieRequest movieRequest){
        logger.info("event=api_create_movie_start title={}",movieRequest.getTitle());
        try{
            Movie createdMovie=movieService.createMovie(movieRequest);
            logger.info("event=api_create_movie_success movieId={}",createdMovie.getId());
            return ResponseEntity.status(HttpStatus.CREATED).body(createdMovie);
        }catch(Exception e){
            logger.error("event=api_create_movie_error title={}",movieRequest.getTitle(),e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    @Operation(summary="Обновить фильм",description="Обновляет фильм")
    public ResponseEntity<Movie> updateMovie(@Parameter(description="ID фильма",example="1")@PathVariable Long id,@Valid@RequestBody MovieRequest movieRequest){
        logger.info("event=api_update_movie_start movieId={}",id);
        try{
            Movie existingMovie=movieService.findById(id).orElseThrow(()->new CustomNotFoundException("Фильм не найден с ID: "+id));
            existingMovie.setTitle(movieRequest.getTitle());
            existingMovie.setDirector(movieRequest.getDirector());
            existingMovie.setReleaseYear(movieRequest.getReleaseYear());
            existingMovie.setGenre(movieRequest.getGenre());
            Movie updatedMovie=movieService.save(existingMovie);
            logger.info("event=api_update_movie_success movieId={}",id);
            return ResponseEntity.ok(updatedMovie);
        }catch(CustomNotFoundException e){
            logger.warn("event=api_update_movie_not_found movieId={}",id);
            return ResponseEntity.notFound().build();
        }catch(Exception e){
            logger.error("event=api_update_movie_error movieId={}",id,e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    @Operation(summary="Удалить фильм",description="Удаляет фильм")
    public ResponseEntity<Void> deleteMovie(@Parameter(description="ID фильма",example="1")@PathVariable Long id){
        logger.info("event=api_delete_movie_start movieId={}",id);
        try{
            movieService.deleteById(id);
            logger.info("event=api_delete_movie_success movieId={}",id);
            return ResponseEntity.noContent().build();
        }catch(CustomNotFoundException e){
            logger.warn("event=api_delete_movie_not_found movieId={}",id);
            return ResponseEntity.notFound().build();
        }catch(Exception e){
            logger.error("event=api_delete_movie_error movieId={}",id,e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}