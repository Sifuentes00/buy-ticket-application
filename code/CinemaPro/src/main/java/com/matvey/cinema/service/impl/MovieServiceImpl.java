package com.matvey.cinema.service.impl;

import com.matvey.cinema.cache.InMemoryCache;
import com.matvey.cinema.model.entities.Movie;
import com.matvey.cinema.repository.FavoriteRepository;
import com.matvey.cinema.repository.MovieRepository;
import com.matvey.cinema.service.MovieService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.matvey.cinema.exception.CustomNotFoundException;
import com.matvey.cinema.model.dto.MovieRequest;
import java.util.List;
import java.util.Optional;

@Service
public class MovieServiceImpl implements MovieService {
    private static final Logger logger=LoggerFactory.getLogger(MovieServiceImpl.class);
    private final MovieRepository movieRepository;
    private final InMemoryCache cache;
    @Autowired
    public MovieServiceImpl(MovieRepository movieRepository,InMemoryCache cache){
        this.movieRepository=movieRepository;
        this.cache=cache;
    }
    @Override
    @Transactional(readOnly=true)
    public Optional<Movie> findById(Long id){
        String cacheKey="movie::id:"+id;
        logger.info("event=movie_find_by_id_start movieId={}",id);
        Optional<Object> cachedData=cache.get(cacheKey);
        if(cachedData.isPresent()){
            logger.debug("event=cache_hit cacheKey={}",cacheKey);
            Object data=cachedData.get();
            if(data instanceof Movie){
                logger.info("event=movie_find_by_id_cache_success movieId={}",id);
                return Optional.of((Movie)data);
            }else{
                cache.evict(cacheKey);
                logger.warn("event=cache_invalid_type cacheKey={}",cacheKey);
            }
        }
        Optional<Movie> movie=movieRepository.findById(id);
        if(movie.isEmpty()){
            logger.warn("event=movie_not_found movieId={}",id);
            return Optional.empty();
        }
        movie.ifPresent(value->{
            cache.put(cacheKey,value);
            logger.debug("event=cache_put cacheKey={} movieId={}",cacheKey,id);
        });
        logger.info("event=movie_find_by_id_success movieId={}",id);
        return movie;
    }
    @Override
    @Transactional(readOnly=true)
    public List<Movie> findAll(){
        String cacheKey="movie::all";
        logger.info("event=movie_find_all_start");
        Optional<Object> cachedData=cache.get(cacheKey);
        if(cachedData.isPresent()){
            logger.debug("event=cache_hit cacheKey={}",cacheKey);
            Object data=cachedData.get();
            if(data instanceof List){
                List<?> list=(List<?>)data;
                if(!list.isEmpty()&&list.get(0) instanceof Movie){
                    logger.info("event=movie_find_all_cache_success");
                    return (List<Movie>)data;
                }else if(list.isEmpty()){
                    return (List<Movie>)data;
                }
            }
            cache.evict(cacheKey);
            logger.warn("event=cache_invalid_type cacheKey={}",cacheKey);
        }
        List<Movie> movies=movieRepository.findAll();
        cache.put(cacheKey,movies);
        logger.debug("event=cache_put cacheKey={}",cacheKey);
        logger.info("event=movie_find_all_success count={}",movies.size());
        return movies;
    }

    @Autowired
    private FavoriteRepository favoriteRepository;

    @Override
    @Transactional
    public Movie save(Movie movie){
        logger.info("event=movie_save_start movieId={}",movie.getId());
        Movie savedMovie=movieRepository.save(movie);
        logger.info("event=movie_saved movieId={}",savedMovie.getId());
        cache.evict("movie::all");
        if(savedMovie.getId()!=null){
            cache.evict("movie::id:"+savedMovie.getId());
        }
        cache.evict("movie::all_with_reviews");
        logger.debug("event=movie_cache_evicted movieId={}",savedMovie.getId());
        return savedMovie;
    }

    @Override
    @Transactional
    public void deleteById(Long id) {
        logger.info("event=movie_delete_start movieId={}", id);
        Optional<Movie> movieOptional = movieRepository.findById(id);
        if (movieOptional.isPresent()) {
            Movie movie = movieOptional.get();
            favoriteRepository.deleteByMovieId(id);


            evictRelatedCache(movie);
            movieRepository.deleteById(id);
            logger.info("event=movie_deleted movieId={}", id);
        } else {
            throw new CustomNotFoundException("Movie not found with ID: " + id);
        }
    }
    private void evictRelatedCache(Movie movie){
        if(movie.getReviews()!=null){
            movie.getReviews().forEach(review->{
                String reviewCacheKey="review::id:"+review.getId();
                cache.evict(reviewCacheKey);
                logger.debug("event=cache_evict reviewId={} cacheKey={}",review.getId(),reviewCacheKey);
            });
        }
        if(movie.getShowtimes()!=null){
            movie.getShowtimes().forEach(showtime->{
                String showtimeCacheKey="showtime::id:"+showtime.getId();
                cache.evict(showtimeCacheKey);
                logger.debug("event=cache_evict showtimeId={} cacheKey={}",showtime.getId(),showtimeCacheKey);
                if(showtime.getTickets()!=null){
                    showtime.getTickets().forEach(ticket->{
                        String ticketCacheKey="ticket::id:"+ticket.getId();
                        cache.evict(ticketCacheKey);
                        logger.debug("event=cache_evict ticketId={} cacheKey={}",ticket.getId(),ticketCacheKey);
                    });
                }
            });
        }
        cache.evict("movie::id:"+movie.getId());
        cache.evict("movie::all");
        cache.evict("movie::all_with_reviews");
        logger.debug("event=movie_cache_evicted movieId={}",movie.getId());
    }
    @Override
    @Transactional(readOnly=true)
    public List<Movie> findAllWithReviews(){
        String cacheKey="movie::all_with_reviews";
        logger.info("event=movie_find_all_with_reviews_start");
        Optional<Object> cachedData=cache.get(cacheKey);
        if(cachedData.isPresent()){
            logger.debug("event=cache_hit cacheKey={}",cacheKey);
            Object data=cachedData.get();
            if(data instanceof List){
                List<?> list=(List<?>)data;
                if(!list.isEmpty()&&list.get(0) instanceof Movie){
                    logger.info("event=movie_find_all_with_reviews_cache_success");
                    return (List<Movie>)data;
                }else if(list.isEmpty()){
                    return (List<Movie>)data;
                }
            }
            cache.evict(cacheKey);
            logger.warn("event=cache_invalid_type cacheKey={}",cacheKey);
        }
        List<Movie> movies=movieRepository.findAllWithReviews();
        cache.put(cacheKey,movies);
        logger.debug("event=cache_put cacheKey={}",cacheKey);
        logger.info("event=movie_find_all_with_reviews_success count={}",movies.size());
        return movies;
    }
    @Override
    @Transactional
    public Movie createMovie(MovieRequest movieRequest){
        logger.info("event=movie_create_start title={}",movieRequest.getTitle());
        Movie newMovie=new Movie();
        newMovie.setTitle(movieRequest.getTitle());
        newMovie.setDirector(movieRequest.getDirector());
        newMovie.setReleaseYear(movieRequest.getReleaseYear());
        newMovie.setGenre(movieRequest.getGenre());
        Movie savedMovie=save(newMovie);
        logger.info("event=movie_created movieId={} title={}",savedMovie.getId(),savedMovie.getTitle());
        return savedMovie;
    }
    @Override
    public void evictMovieCache(Long movieId){
        String cacheKey="movie::id:"+movieId;
        cache.evict(cacheKey);
        logger.debug("event=cache_evict movieId={} cacheKey={}",movieId,cacheKey);
    }
    @Override
    public void evictAllMoviesCache(){
        String cacheKey="movie::all";
        cache.evict(cacheKey);
        logger.debug("event=cache_evict cacheKey={}",cacheKey);
    }
    @Override
    public void evictAllMoviesWithReviewsCache(){
        String cacheKey="movie::all_with_reviews";
        cache.evict(cacheKey);
        logger.debug("event=cache_evict cacheKey={}",cacheKey);
    }
}