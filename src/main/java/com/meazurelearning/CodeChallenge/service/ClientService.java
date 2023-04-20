package com.meazurelearning.CodeChallenge.service;

import com.meazurelearning.CodeChallenge.constants.RabbitConstants;
import com.meazurelearning.CodeChallenge.model.Client;
import com.meazurelearning.CodeChallenge.repository.ClientRepository;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class ClientService {

    private final ClientRepository clientRepository;
    private final RabbitTemplate rabbitTemplate;

    @Autowired
    public ClientService(ClientRepository clientRepository, RabbitTemplate rabbitTemplate) {
        this.clientRepository = clientRepository;
        this.rabbitTemplate = rabbitTemplate;
    }

    public Client createOrUpdateClient(Client client) {
        Optional<Client> existingClient = clientRepository.findByUserIdAndRoomId(client.getUserId(), client.getRoomId());
        if (existingClient.isPresent()) {
            Client updatedClient = existingClient.get();
            updatedClient.setStatus(client.getStatus());
            clientRepository.save(updatedClient);
            rabbitTemplate.convertAndSend(RabbitConstants.CLIENT_UPDATED_ROUTING_KEY, updatedClient);
            return updatedClient;
        } else {
            client.setClientId(UUID.randomUUID());
            clientRepository.save(client);
            rabbitTemplate.convertAndSend(RabbitConstants.CLIENT_CREATED_ROUTING_KEY, client);
            return client;
        }
    }

    public Client updateClient(UUID clientId, Client client) {
        Client existingClient = clientRepository.findById(clientId)
                .orElseThrow(() -> new RuntimeException("Client not found"));

        existingClient.setClientId(clientId);
        existingClient.setUserId(client.getUserId());
        existingClient.setRoomId(client.getRoomId());
        existingClient.setStatus(client.getStatus());

        Client updatedClient = clientRepository.save(existingClient);
        rabbitTemplate.convertAndSend(RabbitConstants.CLIENT_UPDATED_ROUTING_KEY, updatedClient);
        return updatedClient;
    }

    public void deleteClient(UUID clientId) {
        Optional<Client> client = clientRepository.findById(clientId);
        if (client.isPresent()) {
            clientRepository.deleteById(clientId);
            rabbitTemplate.convertAndSend(RabbitConstants.CLIENT_DELETED_ROUTING_KEY, client.get());
        }
    }

    public Client getClient(UUID clientId) {
        return clientRepository.findById(clientId)
                .orElseThrow(() -> new RuntimeException("Client not found"));
    }

    public List<Client> getAllClients() {
        List<Client> clients = clientRepository.findAll();
        if (clients.isEmpty()) {
            throw new RuntimeException("No clients found");
        }
        return clients;
    }
}