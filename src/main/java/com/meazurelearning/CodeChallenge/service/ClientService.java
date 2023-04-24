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

    private Client createAndSaveClient(Client newClient){
        newClient.setClientId(null);

        clientRepository.save(newClient);
        rabbitTemplate.convertAndSend(RabbitConstants.CLIENT_CREATED_ROUTING_KEY, newClient);

        return newClient;
    }

    private Client updateAndSaveClient(Client existingClient, Client newClient) {
        existingClient.setUserId(newClient.getUserId());
        existingClient.setRoomId(newClient.getRoomId());
        existingClient.setStatus(newClient.getStatus());

        clientRepository.save(existingClient);
        rabbitTemplate.convertAndSend(RabbitConstants.CLIENT_UPDATED_ROUTING_KEY, existingClient);

        return existingClient;
    }

    private Optional<Client> findClientById(UUID clientId) {
        return clientRepository.findById(clientId);
    }

    public Client createOrUpdateClient(Client newClient) {
        Optional<Client> existingClient = clientRepository.findByUserIdAndRoomId(newClient.getUserId(), newClient.getRoomId());

        if (existingClient.isPresent()) {
            return this.updateAndSaveClient(existingClient.get(), newClient);
        } else {
            return this.createAndSaveClient(newClient);
        }
    }

    public Optional<Client> updateClient(UUID clientId, Client newClient) {
        return findClientById(clientId).map((existingClient) -> this.updateAndSaveClient(existingClient, newClient));
    }

    public Optional<Client> deleteClient(UUID clientId) {
        return findClientById(clientId).map(existingClient -> {
            clientRepository.deleteById(clientId);
            rabbitTemplate.convertAndSend(RabbitConstants.CLIENT_DELETED_ROUTING_KEY, existingClient);

            return existingClient;
        });
    }

    public Optional<Client> getClient(UUID clientId){
        return this.findClientById(clientId);
    }

    public List<Client> getAllClients() {
        return clientRepository.findAll();
    }

}