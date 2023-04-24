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

    private Client instantiateRandomClient() {
        Client randomClient = new Client();
        randomClient.setUserId(UUID.randomUUID());
        randomClient.setRoomId(UUID.randomUUID());
        randomClient.setStatus(new Random().nextInt(100));

        return randomClient;
    }

    @Test
    void shouldCreateANewClient() {
        Client newClient = instantiateRandomClient();

        when(clientRepository.findByUserIdAndRoomId(newClient.getUserId(), newClient.getRoomId())).thenReturn(Optional.empty());
        when(clientRepository.save(newClient)).thenAnswer(invocation -> {
            Client savedClient = invocation.getArgument(0);
            savedClient.setClientId(UUID.randomUUID());
            return savedClient;
        });

        Client result = clientService.createOrUpdateClient(newClient);

        assertNotNull(result.getClientId());
        assertEquals(result.getUserId(), newClient.getUserId());
        assertEquals(result.getRoomId(), newClient.getRoomId());
        assertEquals(result.getStatus(), newClient.getStatus());

        verify(clientRepository).findByUserIdAndRoomId(newClient.getUserId(), newClient.getRoomId());
        verify(clientRepository).save(newClient);
        verify(rabbitTemplate).convertAndSend(RabbitConstants.CLIENT_CREATED_ROUTING_KEY, result);
        verify(rabbitTemplate, never()).convertAndSend(RabbitConstants.CLIENT_UPDATED_ROUTING_KEY, result);
    }

    @Test
    void shouldUpdateAClientInsteadOfCreateANewOne() {
        UUID existingClientId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID roomId = UUID.randomUUID();

        Client existingClient = new Client(userId, roomId, 1);
        existingClient.setClientId(existingClientId);

        Client newClient = new Client(userId, roomId, 2);

        when(clientRepository.findByUserIdAndRoomId(userId, roomId)).thenReturn(Optional.of(existingClient));
        when(clientRepository.save(existingClient)).thenReturn(existingClient);

        Client result = clientService.createOrUpdateClient(newClient);

        assertEquals(result.getClientId(), existingClientId);
        assertEquals(result.getUserId(), userId);
        assertEquals(result.getRoomId(), roomId);
        assertEquals(result.getStatus(), 2);

        verify(clientRepository).findByUserIdAndRoomId(userId, roomId);
        verify(clientRepository).save(existingClient);
        verify(rabbitTemplate).convertAndSend(RabbitConstants.CLIENT_UPDATED_ROUTING_KEY, result);
        verify(rabbitTemplate, never()).convertAndSend(RabbitConstants.CLIENT_CREATED_ROUTING_KEY, result);
    }

    @Test
    public void shouldUpdateTheClientSucessfully() {
        UUID existingClientId = UUID.randomUUID();

        Client existingClient = new Client(UUID.randomUUID(), UUID.randomUUID(), 1);
        existingClient.setClientId(existingClientId);

        Client updatingClient = new Client(UUID.randomUUID(),UUID.randomUUID(),2);

        when(clientRepository.findById(existingClient.getClientId())).thenReturn(Optional.of(existingClient));
        when(clientRepository.save(existingClient)).thenReturn(existingClient);

        Optional<Client> result = clientService.updateClient(existingClientId, updatingClient);

        assertEquals(result.get().getClientId(), existingClientId);
        assertEquals(result.get().getUserId(), updatingClient.getUserId());
        assertEquals(result.get().getRoomId(), updatingClient.getRoomId());
        assertEquals(result.get().getStatus(), 2);

        verify(clientRepository).findById(existingClientId);
        verify(clientRepository).save(existingClient);
        verify(rabbitTemplate).convertAndSend(RabbitConstants.CLIENT_UPDATED_ROUTING_KEY, result.get());
    }

    @Test
    public void shouldNotUpdateWhenNotFindingAClient() {
        UUID clientId = UUID.randomUUID();

        Client updatingClient = instantiateRandomClient();

        when(clientRepository.findById(clientId)).thenReturn(Optional.empty());


        Optional<Client> result = clientService.updateClient(clientId, updatingClient);

        assertEquals(result, Optional.empty());

        verify(clientRepository).findById(clientId);
        verify(clientRepository, never()).save(any(Client.class));
        verify(rabbitTemplate, never()).convertAndSend(anyString(), any(Client.class));
    }

    @Test
    public void shouldSuccessfullyDeleteAClient() {
        UUID clientId = UUID.randomUUID();

        Client existingClient = instantiateRandomClient();
        existingClient.setClientId(clientId);

        when(clientRepository.findById(clientId)).thenReturn(Optional.of(existingClient));

        Optional<Client> result = clientService.deleteClient(clientId);

        assertEquals(result.get().getClientId(), clientId);
        verify(clientRepository).deleteById(clientId);
        verify(rabbitTemplate).convertAndSend(RabbitConstants.CLIENT_DELETED_ROUTING_KEY, existingClient);
    }

    @Test
    public void shouldNotDeleteWhenNotFindingAClient() {
        UUID clientId = UUID.randomUUID();

        when(clientRepository.findById(clientId)).thenReturn(Optional.empty());

        Optional<Client> result = clientService.deleteClient(clientId);

        assertEquals(result, Optional.empty());
        verify(clientRepository, never()).deleteById(any(UUID.class));
        verify(rabbitTemplate, never()).convertAndSend(anyString(), any(Client.class));
    }

    @Test
    public void shouldSuccessfullyGetAClient() {
        UUID clientId = UUID.randomUUID();

        Client existingClient = instantiateRandomClient();
        existingClient.setClientId(clientId);

        when(clientRepository.findById(clientId)).thenReturn(Optional.of(existingClient));

        Optional<Client> result = clientService.getClient(clientId);

        assertEquals(result.get().getClientId(), clientId);
        assertEquals(result.get().getUserId(), existingClient.getUserId());
        assertEquals(result.get().getRoomId(), existingClient.getRoomId());
        assertEquals(result.get().getStatus(), existingClient.getStatus());

        verify(clientRepository).findById(clientId);
    }

    @Test
    public void shouldGetEmptyClientWhenClientDoNotExists() {
        UUID clientId = UUID.randomUUID();

        when(clientRepository.findById(clientId)).thenReturn(Optional.empty());

        Optional<Client> result = clientService.getClient(clientId);

        assertEquals(result, Optional.empty());
        verify(clientRepository).findById(clientId);
    }

    @Test
    public void shouldGetAllClientsSuccessfully() {
        List<Client> clients = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            Client client = new Client(UUID.randomUUID(), UUID.randomUUID(), i);
            client.setClientId(UUID.randomUUID());

            clients.add(client);
        }

        when(clientRepository.findAll()).thenReturn(clients);

        List<Client> result = clientService.getAllClients();

        assertEquals(result.size(), 5);
        assertEquals(result.get(4).getStatus(), 4);

        verify(clientRepository).findAll();
    }
}