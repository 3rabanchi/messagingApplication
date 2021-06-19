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

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;

@Service
public class RabbitmqService {


    public HashMap<String, Channel> nodes = new HashMap<>();


    public void startConnection(int port) {
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
        nodes.put(String.valueOf(port), channel);
    }

    public void createQueue(String port) throws IOException {
        startConnection(Integer.parseInt(port));
        Channel channel = nodes.get(port);
        channel.queueDeclare("Chat", false, false, false, null);
    }


    public void sendMessage(String port, String queueName, String message) {
        Channel channel = nodes.get(port);
        // channel.queueDeclare(queueName, false, false, false, null);

        checkIfThereAreUnsentMessages(port, channel, queueName);

        try {
            channel.basicPublish("", queueName, null, message.getBytes());
        } catch (Exception e) {
            savingMessageToFile(port, queueName, message);
        }
        System.out.println(" [x] Sent '" + message + "'");
    }

    private void checkIfThereAreUnsentMessages(String port, Channel channel, String queueName) {
        ArrayList<String> lines = new ArrayList<>();
        File myObj = new File("messages_not_sent.txt");
        Scanner myReader = null;
        try {
            myReader = new Scanner(myObj);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        while (myReader.hasNextLine()) {
            lines.add(myReader.nextLine());
        }
        myReader.close();
        boolean flag = true;
        for (String line : lines) {
            line.contains(port);
            try {
                channel.basicPublish("", queueName, null, line.substring(line.lastIndexOf(":")).getBytes());
            } catch (Exception e) {
                e.printStackTrace();
                flag = false;
                break;
            }
            System.out.println("THERE IS AN UNSENT MESSAGE FOR PORT " + port);
        }
        removeSentMessages(port, lines, flag);
    }

    private void removeSentMessages(String port, ArrayList<String> lines, boolean flag) {
        if (flag) {
            try {
                FileWriter myWriter = new FileWriter("messages_not_sent.txt");
                BufferedWriter bw = new BufferedWriter(myWriter);
                for (String line : lines) {
                    if (!line.contains(port)) {
                        bw.write(line);
                        bw.newLine();
                    }
                }
                bw.close();
            } catch (Exception e) {
                System.out.println("Error with file reading!");
            }
        }
    }

    private void savingMessageToFile(String port, String queueName, String message) {
        try {
            System.out.println("Sending failed! Making a backup in file");
            FileWriter myWriter = new FileWriter("messages_not_sent.txt", true);
            BufferedWriter bw = new BufferedWriter(myWriter);
            bw.write(port + ":" + queueName + ":" + message);
            bw.newLine();
            bw.close();
        } catch (Exception e) {
            System.out.println("Error with file reading!");
        }

    }


    public void bindQueue(String port) {
        Channel channel = nodes.get(port);
        try {
            channel.queueBind("Chat", "", "");
        } catch (IOException e) {
            e.printStackTrace();
        }
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
        for (Nodes node : config.getNodes()) {
            System.out.println(node.getManagementPort() + " - checking!");
            if (ifUsed(node.getManagementPort())) {
                System.out.println(node.getManagementPort() + " is not used!");
                nodePort = node.getAmqpPort();
                break;
            } else
                System.out.println(node.getManagementPort() + " is used!");
        }
        return nodePort;
    }
}
