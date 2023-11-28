package ro.ccms.service;

import ro.ccms.domain.*;
import ro.ccms.domain.exceptions.MovieRentalsException;
import ro.ccms.repository.RentalDBRepository;
import ro.ccms.repository.Repository;

import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Predicate;

public class RentalDBService {
    private Repository<Long, Rental> repository;
    private MovieDBService movieService;
    private ClientDBService clientService;


    public RentalDBService(RentalDBRepository repository,
                           MovieDBService movieService,
                           ClientDBService clientService) {
        this.repository = repository;
        this.movieService = movieService;
        this.clientService = clientService;
    }

    /**
     * Retrieve a rent transaction by provided ID.
     *
     * @param id must not be null.
     * @return the requested movie entity.
     * @throws MovieRentalsException if the ID is not found in the Repository.
     */
    public Rental getRentalById(Long id) {
        Optional<Rental> rentalOptional = repository.findOne(id);
        if (rentalOptional.isPresent()) {
            return rentalOptional.get();
        } else {
            throw new MovieRentalsException("Rental transaction with ID " + id + " not found.");
        }
    }

    /**
     * Retrieve all rental transactions.
     *
     * @return all the rental entities.
     */
    public Iterable<Rental> getAllRentals() {
        return repository.findAll();
    }

    /**
     * Creates a new rental entity, representing a rent a movie transaction.
     *
     * @param rental must not be null.
     * @throws MovieRentalsException if Movie or Client ID is not found in the repositories.
     */
    public void rentAMovie(Rental rental) {
        repository.save(rental);
    }

    public void updateRentalTransaction(Rental rental) {
        repository.update(rental);
    }

    /**
     * Delete a rental transaction entity.
     *
     * @param id must not be null.
     */
    public void deleteMovieRental(Long id) {
        Optional<Rental> rentalOptional = repository.delete(id);
        if (rentalOptional.isPresent()) {
            System.out.println("SUCCESS");
            System.out.println("Deleted movie:" + rentalOptional.get());
        } else {
            System.err.println("Rental transaction not found in the database. ");
        }
    }

    /**
     * Sort movies by the number of rents in descending order.
     *
     * @return an ordered list of movie DTO(Movie, counter).
     */
    public Iterable<MovieRentalsDTO> moviesByRentNumber() {
        Map<Long, Integer> mapMovieIdRentCounter = new HashMap<>();
        List<MovieRentalsDTO> moviesByRentCounterDesc = new ArrayList<>();

        Iterable<Rental> rentals = getAllRentals();
        for (Rental r : rentals) {
            Integer counter = 0;
            for (Rental rental : rentals) {
                if (rental.getMovieId() == r.getMovieId()) {
                    counter++;
                }
            }
            mapMovieIdRentCounter.put(r.getMovieId(), counter);
        }

        mapMovieIdRentCounter.forEach((k, v) -> {
            MovieRentalsDTO movieDTO = new MovieRentalsDTO(movieService.getMovieById(k), v);
            if (moviesByRentCounterDesc.isEmpty()) {
                moviesByRentCounterDesc.add(movieDTO);
            } else {
                boolean flag = false;
                for (MovieRentalsDTO m : moviesByRentCounterDesc) {
                    if (m.getRentCounter() <= movieDTO.getRentCounter()) {
                        moviesByRentCounterDesc.add(moviesByRentCounterDesc.indexOf(m), movieDTO);
                        flag = true;
                        break;
                    }
                }
                if (!flag) {
                    moviesByRentCounterDesc.add(movieDTO);
                }
            }
        });
        return moviesByRentCounterDesc;
    }

    /**
     * Sort clients by the number of rented movies in descending order.
     *
     * @return an ordered list of client DTO(Client, counter).
     */
    public List<ClientRentalsDTO> clientsByRentedMovies() {
        Map<Long, Integer> mapClientIdRentedMovies = new HashMap<>();
        List<ClientRentalsDTO> orderedClientsByRentedMovies = new ArrayList<>();
        Iterable<Rental> rentals = getAllRentals();
        for (Rental r : rentals) {
            Long clientId = r.getClientId();
            Integer counter = 0;
            for (Rental rental : rentals) {
                if (rental.getClientId() == clientId) {
                    counter++;
                }
            }
            mapClientIdRentedMovies.put(clientId, counter);
        }

        mapClientIdRentedMovies.forEach((k, v) -> {
            ClientRentalsDTO clientDTO = new ClientRentalsDTO(clientService.getClientById(k), v);
            if (orderedClientsByRentedMovies.isEmpty()) {
                orderedClientsByRentedMovies.add(clientDTO);
            } else {
                boolean flag = false;
                for (ClientRentalsDTO c : orderedClientsByRentedMovies) {
                    if (c.getRentCounter() <= clientDTO.getRentCounter()) {
                        orderedClientsByRentedMovies.add(orderedClientsByRentedMovies.indexOf(c), clientDTO);
                        flag = true;
                        break;
                    }
                }
                if (!flag) {
                    orderedClientsByRentedMovies.add(clientDTO);
                }
            }
        });
        return orderedClientsByRentedMovies;
    }

    /**
     * Generate report of the rented movies, total charges and rent dates for a given client.
     *
     * @param id must not be null.
     * @return the result DTO.
     * @throws IllegalArgumentException if ID is null.
     */
    public ClientRentReportDTO generateReportByClient(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("Id must not be null. ");
        }

        List<Movie> moviesList = new ArrayList<>();
        List<LocalDateTime> rentDates = new ArrayList<>();
        float totalCharges = 0.00f;
        int counter = 0;

        Client client = clientService.getClientById(id);

        Predicate<Rental> clientIdFilter = rental -> rental.getClientId() == id;
        repository.findAll().forEach(rental -> {
            if (clientIdFilter.test(rental)) {
                moviesList.add(movieService.getMovieById(rental.getMovieId()));
                rentDates.add(rental.getRentalDate());
            }
        });

        for (Rental rental :
                repository.findAll()) {
            if (clientIdFilter.test(rental)) {
                totalCharges += rental.getRentalCharge();
                counter++;
            }
        }
        return new ClientRentReportDTO(client, moviesList, totalCharges, rentDates, counter);
    }

    /**
     * Generate report of the clients who rented a given movie, total charges on that movie and the rent dates.
     *
     * @param id must not be null.
     * @return the result DTO.
     * @throws IllegalArgumentException if ID is null
     */
    public MovieRentReportDTO generateReportByMovie(Long id) {
        if (id == null) {
            throw new MovieRentalsException("Id must not be null. ");
        }

        List<Client> clientList = new ArrayList<>();
        List<LocalDateTime> rentDates = new ArrayList<>();
        float totalCharges = 0.00f;
        int counter = 0;

        Movie movie = movieService.getMovieById(id);

        Predicate<Rental> movieIdFilter = rental -> rental.getMovieId() == id;
        repository.findAll().forEach(rental -> {
            if (movieIdFilter.test(rental)) {
                clientList.add(clientService.getClientById(rental.getClientId()));
                rentDates.add(rental.getRentalDate());
            }
        });

        for (Rental rental :
                repository.findAll()) {
            if (movieIdFilter.test(rental)) {
                totalCharges += rental.getRentalCharge();
                counter++;
            }
        }
        return new MovieRentReportDTO(movie, clientList, totalCharges, rentDates, counter);
    }
}
