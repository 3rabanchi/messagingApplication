package com.edcs.service;

import com.edcs.model.Message;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DeliverCallback;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class RabbitmqService {

    public Channel channel;
    public Connection connection;

    public void startConnection() throws Exception{
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
        try {
            connection = factory.newConnection();
            channel = connection.createChannel();
            channel.queueDeclare("First chat", false, false, false, null);
        }
        catch (Exception e){
            System.out.println("An error occured when trying to connect to RabbitMQ, details:"+e.getMessage());
        }
    }
    public void sendMessage(String queueName, String message) throws IOException {
        channel.basicPublish("", queueName, null, message.getBytes());
        System.out.println(" [x] Sent '" + message + "'");
    }

    public void getMessage() throws IOException {
        DeliverCallback deliverCallback = (consumerTag, delivery) -> {
            String message = new String(delivery.getBody(), "UTF-8");
            System.out.println(" [x] Received '" + message + "'");
        };
        channel.basicConsume("First chat", true, deliverCallback, consumerTag -> { });
    }
}
