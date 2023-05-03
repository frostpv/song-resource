package com.example.songsstorage.service;

import model.RabbitMessage;

public interface RabbitService {
    String sendToQueue(RabbitMessage message);
}
