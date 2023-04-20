package com.meazurelearning.CodeChallenge.service;

import com.meazurelearning.CodeChallenge.constants.RabbitConstants;
import com.meazurelearning.CodeChallenge.model.Client;
import com.meazurelearning.CodeChallenge.repository.ClientRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.from;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ClientServiceTest {

    @Mock
    private ClientRepository clientRepository;

    @Mock
    private RabbitTemplate rabbitTemplate;

    @InjectMocks
    private ClientService clientService;

    @Test
    public void shouldCreateANewClient() {
        Client client = new Client(UUID.randomUUID(),UUID.randomUUID(),UUID.randomUUID(),0);

        when(clientRepository.findByUserIdAndRoomId(client.getUserId(), client.getRoomId())).thenReturn(Optional.empty());

        Client result = clientService.createOrUpdateClient(client);

        assertEquals(client, result);

        verify(rabbitTemplate).convertAndSend(RabbitConstants.CLIENT_CREATED_ROUTING_KEY, result);
        verify(rabbitTemplate, never()).convertAndSend(RabbitConstants.CLIENT_UPDATED_ROUTING_KEY, result);
    }

    @Test
    public void shouldUpdateAClientInsteadOfCreateANewOne() {
        Client existingClient = new Client(UUID.randomUUID(),UUID.randomUUID(),UUID.randomUUID(),1);

        Client updatedClient  = new Client(UUID.randomUUID(),existingClient.getUserId(),existingClient.getRoomId(),2);

        when(clientRepository.findByUserIdAndRoomId(existingClient.getUserId(), existingClient.getRoomId())).thenReturn(Optional.of(existingClient));
        when(clientRepository.save(existingClient)).thenReturn(updatedClient);

        Client result = clientService.createOrUpdateClient(updatedClient);

        assertThat(result)
            .returns(existingClient.getClientId(), from(Client::getClientId))
            .returns(existingClient.getUserId(), from(Client::getUserId))
            .returns(existingClient.getRoomId(), from(Client::getRoomId))
            .returns(updatedClient.getStatus(), from(Client::getStatus));

        verify(rabbitTemplate).convertAndSend(RabbitConstants.CLIENT_UPDATED_ROUTING_KEY, result);
        verify(rabbitTemplate, never()).convertAndSend(RabbitConstants.CLIENT_CREATED_ROUTING_KEY, result);
    }

    @Test
    public void shouldUpdateTheClientStatusSucessfully() {
        Client existingClient = new Client(UUID.randomUUID(),UUID.randomUUID(),UUID.randomUUID(),1);

        Client updatedClient = new Client(existingClient.getClientId(),UUID.randomUUID(),UUID.randomUUID(),2);

        when(clientRepository.findById(existingClient.getClientId())).thenReturn(Optional.of(existingClient));
        when(clientRepository.save(existingClient)).thenReturn(updatedClient);

        Client result = clientService.updateClient(existingClient.getClientId(), updatedClient);

        assertEquals(updatedClient, result);

        verify(clientRepository).save(existingClient);
        verify(rabbitTemplate).convertAndSend(RabbitConstants.CLIENT_UPDATED_ROUTING_KEY, updatedClient);
    }

    @Test
    public void shouldGetErrorClientNotFoundWhenTryingToUpdate() {
        Client updatedClient = new Client(UUID.randomUUID(),UUID.randomUUID(),UUID.randomUUID(),2);

        when(clientRepository.findById(updatedClient.getClientId())).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> clientService.updateClient(updatedClient.getClientId(), updatedClient));

        verify(clientRepository).findById(updatedClient.getClientId());
        verify(clientRepository, never()).save(updatedClient);
        verify(rabbitTemplate, never()).convertAndSend(RabbitConstants.CLIENT_CREATED_ROUTING_KEY, updatedClient);
    }

    @Test
    public void shouldSuccessfullyDeleteAClient() {
        Client existingClient = new Client(UUID.randomUUID(),UUID.randomUUID(),UUID.randomUUID(),1);

        when(clientRepository.findById(existingClient.getClientId())).thenReturn(Optional.of(existingClient));

        clientService.deleteClient(existingClient.getClientId());

        verify(clientRepository).deleteById(existingClient.getClientId());
        verify(rabbitTemplate).convertAndSend(RabbitConstants.CLIENT_DELETED_ROUTING_KEY, existingClient);
    }

    @Test
    public void shouldFailToDeleteANonexistentClient() {
        Client nonexistentClient = new Client(UUID.randomUUID(),UUID.randomUUID(),UUID.randomUUID(),0);

        when(clientRepository.findById(nonexistentClient.getClientId())).thenReturn(Optional.empty());

        clientService.deleteClient(nonexistentClient.getClientId());

        verify(clientRepository, never()).deleteById(nonexistentClient.getClientId());
        verify(rabbitTemplate, never()).convertAndSend(RabbitConstants.CLIENT_DELETED_ROUTING_KEY, nonexistentClient);
    }

    @Test
    public void shouldSuccessfullyGetAClient() {
        Client existingClient = new Client(UUID.randomUUID(),UUID.randomUUID(),UUID.randomUUID(),1);

        when(clientRepository.findById(existingClient.getClientId())).thenReturn(Optional.of(existingClient));

        Client result = clientService.getClient(existingClient.getClientId());

        verify(clientRepository).findById(existingClient.getClientId());
        assertEquals(existingClient, result);
    }

    @Test
    public void shouldFailToGetANonexistentClient() {
        UUID nonexistentClientID = UUID.randomUUID();

        when(clientRepository.findById(nonexistentClientID)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> clientService.getClient(nonexistentClientID));

        verify(clientRepository).findById(nonexistentClientID);
    }

    @Test
    public void shouldGetAllClientsSuccessfully() {
        List<Client> clients = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            Client client = new Client(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), i);
            clients.add(client);
        }

        when(clientRepository.findAll()).thenReturn(clients);

        List<Client> result = clientService.getAllClients();

        verify(clientRepository).findAll();
        assertEquals(clients, result);
    }

    @Test
    public void shouldFailToGetAllClientsDueToNotExistingAny() {
        when(clientRepository.findAll()).thenReturn(Collections.emptyList());

        assertThrows(RuntimeException.class, () -> clientService.getAllClients());

        verify(clientRepository).findAll();
    }
}