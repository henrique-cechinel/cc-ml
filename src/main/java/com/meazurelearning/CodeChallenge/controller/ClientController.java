package com.meazurelearning.CodeChallenge.controller;

import com.meazurelearning.CodeChallenge.model.Client;
import com.meazurelearning.CodeChallenge.service.ClientService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/clients")
public class ClientController {

    private final ClientService clientService;

    @Autowired
    public ClientController(ClientService clientService) {
        this.clientService = clientService;
    }

    @PostMapping
    public ResponseEntity<Client> createOrUpdateClient(@RequestBody Client client) {
        Client createdOrUpdateClient = clientService.createOrUpdateClient(client);
        return ResponseEntity.ok(createdOrUpdateClient);
    }

    @PutMapping("/{clientId}")
    public ResponseEntity<Client> updateClient(@PathVariable UUID clientId, @RequestBody Client client) {
        Client updatedClient = clientService.updateClient(clientId, client);
        return new ResponseEntity<>(updatedClient, HttpStatus.OK);
    }

    @DeleteMapping("/{clientId}")
    public ResponseEntity<Void> deleteClient(@PathVariable UUID clientId) {
        clientService.deleteClient(clientId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{clientId}")
    public ResponseEntity<Client> getClient(@PathVariable UUID clientId) {
        Client client = clientService.getClient(clientId);
        return new ResponseEntity<>(client, HttpStatus.OK);
    }

    @GetMapping
    public ResponseEntity<List<Client>> getAllClients() {
        List<Client> clients = clientService.getAllClients();
        return ResponseEntity.ok(clients);
    }
}