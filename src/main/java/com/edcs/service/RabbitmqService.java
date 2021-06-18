package com.edcs.service;

import com.edcs.model.Config;
import com.edcs.model.Nodes;
import com.edcs.utils.FileUtils;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DeliverCallback;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

@Service
public class RabbitmqService {


    public HashMap<String,Channel> nodes = new HashMap<>();


    public Channel startConnection(int port) throws Exception {
        ConnectionFactory factory = new ConnectionFactory();
        Connection connection = null;
        Channel channel = null;
        factory.setHost("localhost");
        factory.setPort(port);
        factory.setRequestedHeartbeat(10);
        try {
            connection = factory.newConnection();
            channel = connection.createChannel();
            System.out.println("Connection with port:" + String.valueOf(port) + " is initialized!");
        } catch (Exception e) {
            System.out.println("An error occured when trying to connect to RabbitMQ, details:" + e.getMessage());
        }
        nodes.put(String.valueOf(port),channel);
        return channel;
    }

    public void sendMessage(String port,String queueName, String message) throws IOException {
        Channel channel = nodes.get(port);
       // channel.queueDeclare(queueName, false, false, false, null);
        channel.basicPublish("", queueName, null, message.getBytes());
        System.out.println(" [x] Sent '" + message + "'");
    }

    public void getMessage() throws IOException {
//        DeliverCallback deliverCallback = (consumerTag, delivery) -> {
//            String message = new String(delivery.getBody(), "UTF-8");
//            System.out.println(" [x] Received '" + message + "'");
//        };
//      //  channel.basicConsume("First chat", true, deliverCallback, consumerTag -> {
//        });
    }

    public boolean ifUsed(String port) {
        String username = "guest";
        String password = "guest";
        HttpHeaders headers = new HttpHeaders();
        headers.setBasicAuth(username, password);
        RestTemplate restTemplate = new RestTemplate();
        String url = "http://localhost:" + Integer.parseInt(port) + "/api/queues";
        HttpEntity<String> request = new HttpEntity<String>(headers);
        ResponseEntity<String> response
                = restTemplate.exchange(url, HttpMethod.GET, request, String.class);
        return response.getBody().toString().equals("[]");
    }

    public String getFreeNodePort() throws JsonProcessingException {
        String data = FileUtils.getConfigFile();

        ObjectMapper mapper = new ObjectMapper();
        Config config = mapper.readValue(data, Config.class);

        String nodePort = null;
        for(Nodes node : config.getNodes()){
            System.out.println(node.getManagementPort()+ " - checking!");
            if(ifUsed(node.getManagementPort())){
                System.out.println(node.getManagementPort()+ " is not used!");
                nodePort = node.getManagementPort();
                break;
            }
            else
                System.out.println(node.getManagementPort()+ " is used!");
        }
        return nodePort;
    }
}
