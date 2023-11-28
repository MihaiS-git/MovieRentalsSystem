package ro.ccms.service;

import ro.ccms.domain.Movie;
import ro.ccms.domain.exceptions.MovieNotFoundException;
import ro.ccms.domain.exceptions.MovieRentalsException;
import ro.ccms.repository.MovieDBRepository;
import ro.ccms.repository.Repository;

import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class MovieDBService {
    private Repository<Long, Movie> repository;

    public MovieDBService(MovieDBRepository repository) {
        this.repository = repository;
    }

    /**
     * Adds a new movie to the database.
     *
     * @param movie must not be null.
     */
    public void addMovie(Movie movie) {
        repository.save(movie);
    }

    /**
     * Retrieve all the Movies from the repository.
     *
     * @return all the movies.
     */
    public Iterable<Movie> getAllMovies() {
        return repository.findAll();
    }

    /**
     * Retrieve the movie with the given {@code id} from the repository.
     *
     * @param id must not be null.
     * @return the requested movie entity.
     * @throws MovieRentalsException if the Movie is not found in the Repository.
     */
    public Movie getMovieById(Long id) {
        Optional<Movie> movieOptional = repository.findOne(id);
        if (movieOptional.isPresent()) {
            return movieOptional.get();
        } else {
            throw new MovieNotFoundException("There is no Movie with Id: " + id + ". ");
        }
    }

    /**
     * Delete movie by given {@code id}.
     *
     * @param id must not be null.
     * @throws MovieRentalsException if id is null
     *                               or if there are database connection problems.
     */
    public void deleteMovieById(Long id) {
            Optional<Movie> movieToDelete = repository.delete(id);
            if (movieToDelete.isPresent()) {
                System.out.println("SUCCESS");
                System.out.println("Deleted movie:" + movieToDelete.get());
            } else {
                System.err.println("Movie not found in the database. ");
            }
    }

    /**
     * Updated the given {@code movie}.
     *
     * @param movie must not be null.
     */
    public void updateMovie(Movie movie) {
        repository.update(movie);
    }

    /**
     * Filter the Movies by a given {@code s}.
     *
     * @param s must not be null.
     * @return a set of the filtered Movies.
     * @throws MovieRentalsException if there are movie validation problems
     *                               or if there are database connection problems.
     */
    public Set<Movie> filterMoviesByKeyword(String s) {
        try {
            Iterable<Movie> movies = repository.findAll();
            return StreamSupport.stream(movies.spliterator(), false)
                    .filter(movie -> movie.getTitle().contains(s)).collect(Collectors.toSet());
        } catch (MovieRentalsException e) {
            throw new MovieRentalsException("Service exception. " + e.getMessage());
        }
    }
}
