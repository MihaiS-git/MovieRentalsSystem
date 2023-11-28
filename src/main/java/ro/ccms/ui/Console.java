package ro.ccms.ui;

import ro.ccms.domain.*;
import ro.ccms.domain.exceptions.MovieNotFoundException;
import ro.ccms.domain.exceptions.MovieRentalsException;
import ro.ccms.service.ClientDBService;
import ro.ccms.service.MovieDBService;
import ro.ccms.service.RentalDBService;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;

public class Console {
    private MovieDBService movieService;
    private ClientDBService clientService;
    private RentalDBService rentalService;
    private Scanner scanner;

    public Console(MovieDBService movieService, ClientDBService clientService, RentalDBService rentalService) {
        this.movieService = movieService;
        this.clientService = clientService;
        this.rentalService = rentalService;
        this.scanner = new Scanner(System.in);
    }

    /**
     * Print the general menu.
     */
    public void showMenu() {
        System.out.println();
        System.out.println("MENU");
        System.out.println("=".repeat(50));
        System.out.println("1. Movies Menu");
        System.out.println("2. Clients Menu");
        System.out.println("3. Rent Movie & Reports Menu");
        System.out.println("0. Exit");
        System.out.print("\nEnter your option: ");
    }

    /**
     * Run the Console.
     */
    public void runConsole() {
        while (true) {
            this.showMenu();
            if (scanner.hasNextInt()) {
                int option = scanner.nextInt();
                switch (option) {
                    case 1:
                        this.runSubMenuMovies();
                        break;
                    case 2:
                        this.runSubMenuClients();
                        break;
                    case 3:
                        this.runSubMenuRentals();
                        break;
                    case 0:
                        return;
                    default:
                        System.err.println("Unsupported command.");
                }
            } else {
                String invalidInput = scanner.next();
                System.err.println("Invalid input. Please enter a valid number.");
            }
        }
    }

    private void runSubMenuRentals() {
        while (true) {
            System.out.println();
            System.out.println("RENT & REPORTS MENU");
            System.out.println("=".repeat(50));
            System.out.println("1. Print a rent transaction by ID");
            System.out.println("2. Print all rent transactions");
            System.out.println("3. Rent a Movie");
            System.out.println("4. Update a Rent Transaction");
            System.out.println("5. Delete a Rent Transaction");
            System.out.println("6. Print Movies by Rent Counter");
            System.out.println("7. Print Clients by Number of Rented Movies");
            System.out.println("8. Print Client Rent Report by ID");
            System.out.println("9. Print Movie Rent Report by ID");
            System.out.println("0. Back");
            System.out.print("\nEnter your option: ");

            if (scanner.hasNextInt()) {
                int option = scanner.nextInt();
                switch (option) {
                    case 1:
                        this.handlePrintRental();
                        break;
                    case 2:
                        this.handlePrintAllRentals();
                        break;
                    case 3:
                        this.handleRentAMovie();
                        break;
                    case 4:
                        this.handleUpdateRentTransaction();
                        break;
                    case 5:
                        this.handleDeleteRentTransaction();
                        break;
                    case 6:
                        this.handleMoviesByRentals();
                        break;
                    case 7:
                        this.handleClientsByRentedMovies();
                        break;
                    case 8:
                        this.handleClientRentReport();
                        break;
                    case 9:
                        this.handleMovieRentReport();
                        break;
                    case 0:
                        return;
                    default:
                        System.err.println("Unsupported command.");
                }
            } else {
                String invalidInput = scanner.next();
                System.err.println("Invalid input. Please enter a valid number!");
            }
        }
    }

    private void handleMovieRentReport() {
        Long id = null;
        while (id == null) {
            System.out.print("Enter the ID of the Movie: ");
            if (scanner.hasNextInt()) {
                id = scanner.nextLong();
            } else {
                scanner.next();
                System.out.println("Invalid input. Please enter a valid ID");
            }
        }

        try {
            MovieRentReportDTO movieDTO = rentalService.generateReportByMovie(id);
            System.out.println("\nMOVIE #" + id + " RENT REPORT");
            System.out.println("*".repeat(50));
            System.out.println("Movie information: " + movieDTO.getMovie());
            System.out.println("List of Clients: " + movieDTO.getClientsList());
            System.out.println("Total Charges: $" + movieDTO.getTotalCharges());
            System.out.println("Rent Dates List: " + movieDTO.getRentDates());
            System.out.println("Total number of rents: " + movieDTO.getCounter());
        } catch (MovieRentalsException e) {
            System.err.println("An error occurred: " + e.getMessage());
        }
    }

    private void handleClientRentReport() {
        Long id = null;
        while (id == null) {
            System.out.print("Enter the ID of the Client: ");
            if (scanner.hasNextLong()) {
                id = scanner.nextLong();
            } else {
                scanner.next();
                System.err.println("Invalid input. Please enter a valid ID.");
            }
        }
        try {
            ClientRentReportDTO clientReport = rentalService.generateReportByClient(id);
            System.out.println("\nCLIENT #" + id + " RENT REPORT");
            System.out.println("*".repeat(50));
            System.out.println("Client information: " + clientReport.getClient());
            System.out.println("List of rented Movies: " + clientReport.getMoviesList());
            System.out.println("Total Charges: $" + clientReport.getTotalCharges());
            System.out.println("Rent Dates List: " + clientReport.getRentDates());
            System.out.println("Total Number of Rents: " + clientReport.getCounter());
        } catch (MovieRentalsException e) {
            System.err.println("An error occurred: " + e.getMessage());
        }
    }

    private void handleUpdateRentTransaction() {
        Long id = null;
        while (id == null) {
            System.out.print("Enter the ID of the Rent Transaction: ");
            if (scanner.hasNextLong()) {
                id = scanner.nextLong();
            } else {
                scanner.next();
                System.err.println("Invalid input. Please enter a valid ID.");
            }
        }
        while (rentalService.getRentalById(id) != null) {
            Rental rental = readRentTransaction();
            rental.setId(id);
            try {
                rentalService.updateRentalTransaction(rental);
                break;
            } catch (IllegalArgumentException | MovieRentalsException e) {
                System.err.println("An error occurred: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    private void handleDeleteRentTransaction() {
        Long id = null;
        while (id == null) {
            System.out.print("Enter the ID of the Rent Transaction: ");
            if (scanner.hasNextLong()) {
                id = scanner.nextLong();
            } else {
                scanner.next();
                System.err.println("Invalid input. Please enter a valid ID.");
            }
        }
        try {
            rentalService.deleteMovieRental(id);
        } catch (IllegalArgumentException | MovieRentalsException e) {
            System.err.println("An error occurred: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void handleClientsByRentedMovies() {
        try {
            rentalService.clientsByRentedMovies().forEach(System.out::println);
        } catch (IllegalArgumentException | MovieRentalsException e) {
            System.err.println("An error occurred: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void handleMoviesByRentals() {
        try {
            rentalService.moviesByRentNumber().forEach(System.out::println);
        } catch (IllegalArgumentException | MovieRentalsException e) {
            System.err.println("An error occurred: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void handleRentAMovie() {
        try {
            Rental rental = readRentTransaction();
            try {
                rentalService.rentAMovie(rental);
            } catch (IllegalArgumentException | MovieRentalsException e) {
                System.err.println("An error occurred: " + e.getMessage());
                e.printStackTrace();
            }
        } catch (MovieRentalsException e) {
            System.err.println("An error occurred: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private Rental readRentTransaction() {
        // Read Client ID
        System.out.print("Enter Client ID: ");
        Long clientId = null;
        while (clientId == null) {
            if (scanner.hasNextLong()) {
                clientId = scanner.nextLong();
            } else {
                scanner.next();
                System.err.println("Invalid input. Please enter a valid ID.");
            }
        }
        if (clientService.getClientById(clientId) == null) {
            throw new MovieRentalsException("Client not found in the Repository.");
        }

        // Read Movie ID
        System.out.print("Enter Movie ID: ");
        Long movieId = null;
        while (movieId == null) {
            if (scanner.hasNextLong()) {
                movieId = scanner.nextLong();
            } else {
                scanner.next();
                System.err.println("Invalid input. Please enter a valid ID.");
            }
        }
        if (movieService.getMovieById(movieId) == null) {
            throw new MovieRentalsException("Movie not found in the Repository.");
        }

        float rentalCharge = movieService.getMovieById(clientId).getRentalPrice();
        LocalDateTime rentalDate = LocalDateTime.now();
        LocalDateTime dueDate = rentalDate.plusDays(1);

        return new Rental(clientId, clientId, rentalCharge, rentalDate, dueDate);
    }

    private void handlePrintAllRentals() {
        try {
            Iterable<Rental> rentals = rentalService.getAllRentals();
            rentals.forEach(System.out::println);
        } catch (MovieRentalsException e) {
            System.err.println("An error occurred: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void handlePrintRental() {
        System.out.print("Enter the id of the Rental transaction: ");
        Long id = null;
        while (id == null) {
            if (scanner.hasNextLong()) {
                id = scanner.nextLong();
            } else {
                scanner.next();
                System.err.println("Invalid input. Please enter a valid ID.");
            }
        }
        try {
            Rental rental = rentalService.getRentalById(id);
            System.out.println(rental);
        } catch (IllegalArgumentException | MovieRentalsException e) {
            System.err.println("An error occurred: " + e.getMessage());
        }
    }

    /**
     * Run Clients SubMenu
     */
    private void runSubMenuClients() {
        while (true) {
            System.out.println();
            System.out.println("CLIENTS MENU");
            System.out.println("=".repeat(50));
            System.out.println("1. Add Client");
            System.out.println("2. Print Client");
            System.out.println("3. Print All Clients");
            System.out.println("4. Update Client");
            System.out.println("5. Delete Client");
            System.out.println("6. Filter Client by Last Name");
            System.out.println("7. Generate Clients Report");
            System.out.println("0. Back");
            System.out.print("\nEnter your option: ");

            if (scanner.hasNextInt()) {
                int option = scanner.nextInt();
                switch (option) {
                    case 1:
                        this.handleAddClient();
                        break;
                    case 2:
                        this.handlePrintClient();
                        break;
                    case 3:
                        this.handleGetAllClients();
                        break;
                    case 4:
                        this.handleUpdateClient();
                        break;
                    case 5:
                        this.handleDeleteClientByID();
                        break;
                    case 6:
                        this.handleFilterClientsByKeyword();
                        break;
                    case 7:
                        this.handleGenerateClientsReport();
                        break;
                    case 0:
                        return;
                    default:
                        System.err.println("Unsupported command.");
                }
            } else {
                String invalidInput = scanner.next();
                System.err.println("Invalid input. Please enter a valid number!");
            }
        }
    }

    /**
     * Handle Client Report
     */
    private void handleGenerateClientsReport() {
        try {
            Map<String, Boolean> clientReport = clientService.generateClientsReport();
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("\n====================Clients Report=====================\n".toUpperCase()).append("-".repeat(55)).append("\n");
            stringBuilder.append(String.format("|%-30s | %20s|%n", "CLIENT LAST NAME", "SUBSCRIBE"));
            stringBuilder.append("-".repeat(55)).append("\n");
            clientReport.forEach((i, j) -> {
                stringBuilder.append(String.format("|%-30s | %20s|%n", i, j));
                stringBuilder.append("-".repeat(55)).append("\n");
            });
            String report = stringBuilder.toString();
            System.out.println(report);
        } catch (MovieRentalsException e) {
            System.err.println("An error occurred: " + e.getMessage());
        }
    }

    private void handleFilterClientsByKeyword() {
        System.out.print("Enter a keyword to filter: ");
        String keyword = scanner.next();
        try {
            Set<Client> filteredClients = clientService.filterClientsByKeyword(keyword);

            System.out.println("Filtered Clients by keyword: " + keyword);
            System.out.println("=".repeat(50));
            filteredClients.forEach(System.out::println);
        } catch (MovieRentalsException e) {
            System.err.println("An error occurred: " + e.getMessage());
        }
    }

    /**
     * handle Delete Client Feature
     */
    private void handleDeleteClientByID() {
        System.out.print("Enter the id of the Client: ");
        Long id = null;
        while (id == null) {
            if (scanner.hasNextLong()) {
                id = scanner.nextLong();
            } else {
                scanner.next();
                System.err.println("Invalid input. Please enter a valid ID.");
            }
        }
        try {
            clientService.deleteClientById(id);
        } catch (IllegalArgumentException | MovieRentalsException e) {
            System.err.println("An error occurred: " + e.getMessage());
        }
    }

    /**
     * Handle Update Client Feature
     */
    private void handleUpdateClient() {
        System.out.print("Enter the id of the client: ");
        Long id = null;
        while (id == null) {
            if (scanner.hasNextLong()) {
                id = scanner.nextLong();
            } else {
                scanner.next();
                System.err.println("Invalid input. Please enter a valid ID.");
            }
        }
        while (clientService.getClientById(id) != null) {
            Client client = readClient();
            client.setId(id);
            try {
                clientService.updateClient(client);
                break;
            } catch (IllegalArgumentException | MovieRentalsException e) {
                System.err.println(e.getMessage());
            }
        }
    }

    /**
     * Handle Print Client Feature
     */
    private void handlePrintClient() {
        System.out.print("Enter the id of the Client: ");
        Long id = null;
        while (id == null) {
            if (scanner.hasNextLong()) {
                id = scanner.nextLong();
            } else {
                scanner.next();
                System.err.println("Invalid input. Please enter a valid ID.");
            }
        }

        try {
            Client client = clientService.getClientById(id);
            System.out.println(client);
        } catch (IllegalArgumentException | MovieRentalsException e) {
            System.err.println("An error occurred: " + e.getMessage());
        }
    }


    /**
     * Handle Get All Clients Feature
     */
    private void handleGetAllClients() {
        try {
            Iterable<Client> clients = clientService.getAllClients();
            clients.forEach(System.out::println);
        } catch (MovieRentalsException e) {
            System.err.println("An error occurred: " + e.getMessage());
        }
    }

    /**
     * Handle Add Client Feature
     */
    private void handleAddClient() {

        try {
            Client client = readClient();
            try {
                clientService.addClient(client);
            } catch (MovieRentalsException e) {
                System.err.println("An error occurred: " + e.getMessage());
            }
        } catch (IllegalArgumentException e) {
            System.err.println(e.getMessage());
        }
    }

    private Client readClient() {

        scanner.nextLine();
        //Read First Name of the Client
        System.out.print("Enter firstName: ");
        String firstName = scanner.nextLine();

        //Read Last Name of the Client
        System.out.print("Enter lastName: ");
        String lastName = scanner.nextLine();

        //Read Date Of Birth of the Client by format yyy-MM-dd
        System.out.print("Enter dateOfBirth:yyyy-MM-dd: ");
        String dateOfBirth = scanner.nextLine();

        //Read the email of the Client
        System.out.print("Enter email: ");
        String email = scanner.nextLine();

        //Read Boolean for subscribe of the Client
        System.out.print("Do you want to subscribe?:true/false ");
        Boolean subscribe = scanner.nextBoolean();

        return new Client(firstName, lastName, dateOfBirth, email, subscribe);
    }

    /**
     * Run the Movies submenu.
     */
    private void runSubMenuMovies() {
        while (true) {
            System.out.println();
            System.out.println("MOVIES MENU");
            System.out.println("=".repeat(50));
            System.out.println("1. Add Movie");
            System.out.println("2. Print Movie");
            System.out.println("3. Print All Movies");
            System.out.println("4. Update Movie");
            System.out.println("5. Delete Movie");
            System.out.println("6. Filter Movies by Keyword");
            System.out.println("0. Back");
            System.out.print("\nEnter your option: ");

            if (scanner.hasNextInt()) {
                int option = scanner.nextInt();
                switch (option) {
                    case 1:
                        this.handleAddMovie();
                        break;
                    case 2:
                        this.handlePrintMovie();
                        break;
                    case 3:
                        this.handleGetAllMovies();
                        break;
                    case 4:
                        this.handleUpdateMovie();
                        break;
                    case 5:
                        this.handleDeleteMovieById();
                        break;
                    case 6:
                        this.handleFilterMoviesByKeyword();
                        break;
                    case 0:
                        return;
                    default:
                        System.err.println("Unsupported command.");
                }
            } else {
                String invalidInput = scanner.next();
                System.err.println("Invalid input. Please enter a valid number");
            }
        }
    }

    /**
     * Handle filter Movies by Keyword.
     */
    private void handleFilterMoviesByKeyword() {
        System.out.print("Enter the filter Keyword: ");
        String keyword = scanner.next();

        try {
            Set<Movie> filteredMovies = movieService.filterMoviesByKeyword(keyword);

            System.out.println("Filtered movies by keyword: " + keyword);
            System.out.println("=".repeat(50));
            filteredMovies.forEach(System.out::println);
        } catch (MovieRentalsException e) {
            System.err.println("An error occurred: " + e.getMessage());
        }
    }

    /**
     * Handle update Movie feature
     */
    private void handleUpdateMovie() {
        System.out.print("Enter the ID of the Movie: ");
        Long id = null;
        while (id == null) {
            if (scanner.hasNextLong()) {
                id = scanner.nextLong();
            } else {
                scanner.next();
                System.err.println("Invalid input. Please enter a valid ID.");
            }
        }
        try {
            while (movieService.getMovieById(id) != null) {
                try {
                    Movie movie = readMovie();
                    movie.setId(id);
                    try {
                        movieService.updateMovie(movie);
                        break;
                    } catch (IllegalArgumentException | MovieRentalsException e) {
                        System.err.println("An error occurred: " + e.getMessage());
                        e.printStackTrace();
                    }
                } catch (IOException e) {
                    System.err.println(e.getMessage());
                    e.printStackTrace();
                }
            }
        } catch (MovieRentalsException e) {
            System.err.println("An error occurred: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Handle delete Movie feature.
     */
    private void handleDeleteMovieById() {
        System.out.print("Enter the id of the Movie: ");
        Long id = null;
        while (id == null) {
            if (scanner.hasNextLong()) {
                id = scanner.nextLong();
            } else {
                scanner.next();
                System.err.println("Invalid input. Please enter a valid ID.");
            }
        }
        try {
            movieService.deleteMovieById(id);
        } catch (IllegalArgumentException | MovieRentalsException e) {
            System.err.println("An error occurred: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Handle print Movie by ID feature.
     */
    private void handlePrintMovie() {
        System.out.print("Enter the id of the Movie: ");
        Long id = null;
        while (id == null) {
            if (scanner.hasNextLong()) {
                id = scanner.nextLong();
            } else {
                scanner.next();
                System.err.println("Invalid input. Please enter a valid ID.");
            }
        }
        try {
            Movie movie = movieService.getMovieById(id);
            System.out.println(movie);
        } catch (MovieNotFoundException e) {
            System.err.println(e + " Please try with a valid ID.");
        } catch (IllegalArgumentException | MovieRentalsException e) {
            System.err.println("An error occurred: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Handle Get All Movies feature.
     */
    private void handleGetAllMovies() {
        try {
            Iterable<Movie> movies = movieService.getAllMovies();
            movies.forEach(System.out::println);
        } catch (MovieRentalsException e) {
            System.err.println("An error occurred: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Handle Add Movie feature.
     */
    private void handleAddMovie() {
        try {
            Movie movie = readMovie();
            try {
                movieService.addMovie(movie);
                System.out.println("\nSuccess. New movie added in the Database.");
            } catch (IllegalArgumentException | MovieRentalsException e) {
                System.err.println("An error occurred: " + e.getMessage());
                e.printStackTrace();
            }
        } catch (IOException ioe) {
            System.err.println("IO Exception: " + ioe.getMessage());
            ioe.printStackTrace();
        }
    }

    /**
     * Read user entered info for a Movie.
     *
     * @return a Movie entity.
     */
    private Movie readMovie() throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));

        // Read Movie Title
        String title = "";
        while (title.equals("")) {
            System.out.print("Enter the movie title: ");
            title = reader.readLine().trim();
        }

        // Read Movie Year
        int year = 0;
        boolean validYear = false;
        while (!validYear) {
            System.out.print("Enter the year of the movie: ");
            try {
                year = Integer.parseInt(reader.readLine().trim());
                validYear = true;
            } catch (NumberFormatException e) {
                System.err.println("Invalid input. Please enter a valid (int) year. ");
            }
        }

        // Read Movie Genre
        MovieGenres genre = null;
        while (genre == null) {
            System.out.print("Enter the genre of the movie" +
                    "(Action/Comedy/Drama/Fantasy/Horror/Mystery/Romance/Thriller/Western): ");
            String genreInput = reader.readLine().trim();
            try {
                genre = MovieGenres.valueOf(genreInput.toUpperCase());
            } catch (IllegalArgumentException e) {
                System.err.println("Invalid input. Please enter a valid Movie genre. ");
            }
        }

        // Read Movie AgeRestriction
        AgeRestrictions ageRestriction = null;
        while (ageRestriction == null) {
            System.out.print("Enter the age restrictions of the movie(GA/PG/PG13/R/NC17): ");
            String ageRestrictionInput = reader.readLine().trim();
            try {
                ageRestriction = AgeRestrictions.valueOf(ageRestrictionInput.toUpperCase());
            } catch (IllegalArgumentException e) {
                System.err.println("Invalid input. Please enter a valid Movie age restriction.");
            }
        }

        // Read Movie price for rent.
        float rentalPrice = 0.0f;
        boolean validRentalPrice = false;
        while (!validRentalPrice) {
            System.out.print("Enter the price for rent of the Movie: ");
            try {
                rentalPrice = Float.parseFloat(reader.readLine().trim());
                validRentalPrice = true;
            } catch (NumberFormatException e) {
                System.err.println("Invalid input. Please enter a valid (Float) price.");
            }
        }

        // Read Movie availability
        boolean available;
        while (true) {
            System.out.print("Is it available for rent?(true/false): ");
            String availableInput = reader.readLine();
            if (availableInput.trim().equalsIgnoreCase("false") ||
                    availableInput.trim().equalsIgnoreCase("true")) {
                available = Boolean.parseBoolean(availableInput);
                break;
            } else {
                System.err.println("Invalid input. Please enter 'true' or 'false'.");
            }
        }

        Movie movie = new Movie(title, year, genre, ageRestriction, rentalPrice, available);
        return movie;
    }
}
