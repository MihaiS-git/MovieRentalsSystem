package ro.ccms;

import ro.ccms.repository.ClientDBRepository;
import ro.ccms.repository.MovieDBRepository;
import ro.ccms.repository.RentalDBRepository;
import ro.ccms.service.ClientDBService;
import ro.ccms.service.MovieDBService;
import ro.ccms.service.RentalDBService;
import ro.ccms.ui.Console;


public class Main {
    public static void main(String[] args){

        MovieDBRepository movieRepository = new MovieDBRepository();
        MovieDBService movieService = new MovieDBService(movieRepository);

        ClientDBRepository clientRepository = new ClientDBRepository();
        ClientDBService clientService = new ClientDBService(clientRepository);

        RentalDBRepository rentalRepository = new RentalDBRepository();
        RentalDBService rentalService = new RentalDBService(rentalRepository, movieService, clientService);

        Console console = new Console(movieService, clientService, rentalService);
        console.runConsole();
    }
}