package ro.ccms.service;

import ro.ccms.domain.Client;
import ro.ccms.domain.exceptions.ClientNotFoundException;
import ro.ccms.repository.ClientDBRepository;
import ro.ccms.repository.Repository;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class ClientDBService {
    private Repository<Long, Client> repository;

    public ClientDBService(ClientDBRepository repository) {
        this.repository = repository;
    }

    /**
     * Adds new client to the repository
     *
     * @param client must not be null
     */
    public void addClient(Client client) {
        repository.save(client);
    }

    /**
     * Retrieve all the Clients from the repository
     */
    public Iterable<Client> getAllClients() {
        return repository.findAll();
    }

    /**
     * Retrieve the client with the give {@code id} from the repository
     *
     * @param id must not be null
     * @return the request client entity
     */
    public Client getClientById(Long id) {
        Optional<Client> clientOptional = repository.findOne(id);
        if (clientOptional.isPresent()) {
            return clientOptional.get();
        } else {
            throw new ClientNotFoundException("There is no Client with Id: " + id);
        }
    }

    /**
     * Updated the given {@code client}
     *
     * @param client must not be null
     */
    public void updateClient(Client client) {
        repository.update(client);
    }

    /**
     * Delete Client by given id
     *
     * @param id must not be null
     */
    public void deleteClientById(Long id) {
        Optional<Client> clientToDelete = repository.delete(id);
        if (clientToDelete.isPresent()) {
            System.out.println("Deleted client: " + clientToDelete.get());
        } else {
            System.err.println("Client not found in database");
        }
    }

    /**
     * Filter the Clients by a given String s
     *
     * @param s must not be null
     * @return a set of filtered Clients
     */
    public Set<Client> filterClientsByKeyword(String s) {
        Iterable<Client> clients = repository.findAll();
        return StreamSupport.stream(clients.spliterator(), false).filter(client -> client.getLastName()
                .contains(s)).collect(Collectors.toSet());
    }

    /**
     * Generates a Client Report
     *
     * @return Map report of LastName, isSubscribe
     */
    public Map<String, Boolean> generateClientsReport() {
        Iterable<Client> clients = getAllClients();
        Map<String, Boolean> clientsReport = new HashMap<>();
        clients.forEach(client -> {
            clientsReport.put(client.getLastName(), client.isSubscribe());
        });
        return clientsReport;
    }
}
