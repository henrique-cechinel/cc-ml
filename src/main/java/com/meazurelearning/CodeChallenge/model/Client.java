package com.meazurelearning.CodeChallenge.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

import java.io.Serializable;
import java.util.UUID;

@Entity
public class Client implements Serializable {

    @Id
    @GeneratedValue(strategy= GenerationType.AUTO)
    private UUID clientId;
    private UUID userId;
    private UUID roomId;
    private Integer status;

    public Client() {}

    public Client(UUID userId, UUID roomId, Integer status) {
        this.userId = userId;
        this.roomId = roomId;
        this.status = status;
    }

    public void setClientId(UUID clientId) {
        this.clientId = clientId;
    }

    public UUID getClientId() {
        return clientId;
    }

    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }

    public UUID getRoomId() {
        return roomId;
    }

    public void setRoomId(UUID roomId) {
        this.roomId = roomId;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }
}