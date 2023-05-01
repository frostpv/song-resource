package com.example.songsstorage.service.impl;

import com.example.songsstorage.config.MQConfig;
import com.example.songsstorage.service.RabbitService;
import model.RabbitMessage;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class RabbitServiceImpl implements RabbitService {

    @Autowired
    private RabbitTemplate template;

    @Override
    public String sendToQueue(RabbitMessage message) {
        template.convertAndSend(MQConfig.EXCHANGE_NAME, MQConfig.ROUTING_KEY, message);
        return "message was sent to rabbit";
    }
}
