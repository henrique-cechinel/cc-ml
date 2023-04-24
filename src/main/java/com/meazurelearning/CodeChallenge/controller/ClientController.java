package com.meazurelearning.CodeChallenge.controller;

import com.meazurelearning.CodeChallenge.model.Client;
import com.meazurelearning.CodeChallenge.service.ClientService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

import static org.springframework.http.HttpStatus.NOT_FOUND;

@RestController
@RequestMapping("/clients")
public class ClientController {

    private final ClientService clientService;

    @Autowired
    public ClientController(ClientService clientService) {
        this.clientService = clientService;
    }

    @PostMapping
    public Client createOrUpdateClient(@RequestBody Client client) {
        return clientService.createOrUpdateClient(client);
    }

    @PutMapping("/{clientId}")
    public Client updateClient(@PathVariable UUID clientId, @RequestBody Client client) {
        return clientService.updateClient(clientId, client)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Client not found!"));
    }

    @DeleteMapping("/{clientId}")
    public Client deleteClient(@PathVariable UUID clientId) {
        return clientService.deleteClient(clientId)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Client not found!"));
    }

    @GetMapping("/{clientId}")
    public Client getClient(@PathVariable UUID clientId) {
        return clientService.getClient(clientId)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Client not found!"));
    }

    @GetMapping
    public List<Client> getAllClients() {
        return clientService.getAllClients();
    }
}