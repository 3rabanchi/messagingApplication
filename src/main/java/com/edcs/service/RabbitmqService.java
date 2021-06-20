package com.edcs.service;

import com.edcs.model.Config;
import com.edcs.model.Message;
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;

@Service
public class RabbitmqService {


    public static final String EXCHANGE_NAME = "Messaging";
    public HashMap<String, Channel> nodes = new HashMap<>();
    public final ArrayList<Message> MESSAGES = new ArrayList<>();

    public void startConnection(int port) {
        ConnectionFactory factory = new ConnectionFactory();
        Connection connection = null;
        Channel channel = null;

        assignHost(port, factory);
        factory.setPort(port);

        factory.setRequestedHeartbeat(10);
        try {
            connection = factory.newConnection();
            channel = connection.createChannel();
            channel.exchangeDeclare(EXCHANGE_NAME, "topic");
            System.out.println("Connection with port:" + String.valueOf(port) + " is initialized!");
        } catch (Exception e) {
            System.out.println("An error occured when trying to connect to RabbitMQ, details:" + e.getMessage());
        }
        nodes.put(String.valueOf(port), channel);
    }

    private void assignHost(int port, ConnectionFactory factory) {
        if(port >1013)
            factory.setHost("192.168.8.156");
        else
            factory.setHost("192.168.8.150");
    }

    public void createQueue(String port) throws IOException {
        startConnection(Integer.parseInt(port));
        Channel channel = nodes.get(port);
        String queueName = "queue." + getConfig().getUser();

        channel.queueDeclare(queueName, false, false, false, null);
        channel.queueBind(queueName, EXCHANGE_NAME, "user.*");
    }


    public void sendMessage(String port, String message) throws JsonProcessingException {

        Channel channel = nodes.get(port);
        checkIfThereAreUnsentMessages(port, channel);

        String messageContent = getConfig().getUser()+":"+message;

        try {
            channel.basicPublish(EXCHANGE_NAME, "user.*", null, messageContent.getBytes());
        } catch (Exception e) {
            savingMessageToFile(port, message);
        }
        System.out.println(" [x] Sent '" + message + "'");
    }

    private void checkIfThereAreUnsentMessages(String port, Channel channel) {
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
                channel.basicPublish(EXCHANGE_NAME, "user.*", null, line.substring(line.lastIndexOf(":")).getBytes());
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

    private void savingMessageToFile(String port, String message) {
        try {
            System.out.println("Sending failed! Making a backup in file");
            FileWriter myWriter = new FileWriter("messages_not_sent.txt", true);
            BufferedWriter bw = new BufferedWriter(myWriter);
            bw.write(port + ":" + message);
            bw.newLine();
            bw.close();
        } catch (Exception e) {
            System.out.println("Error with file reading!");
        }

    }

    public boolean ifUsed(String port) {
        String username = "guest";
        String password = "guest";
        HttpHeaders headers = new HttpHeaders();
        headers.setBasicAuth(username, password);
        RestTemplate restTemplate = new RestTemplate();
        String host;
        if (Integer.parseInt(port)>1003)
            host = "192.168.8.156:"+port;
        else
            host = "192.168.8.150:"+port;

        String url = "http://"+host+"/api/queues".trim();
        HttpEntity<String> request = new HttpEntity<String>(headers);
        ResponseEntity<String> response
                = restTemplate.exchange(url, HttpMethod.GET, request, String.class);
        return response.getBody().toString().equals("[]");
    }

    public String getFreeNodePort() throws JsonProcessingException {
        Config config = getConfig();

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

    public Config getConfig() throws JsonProcessingException {
        String data = FileUtils.getConfigFile();

        ObjectMapper mapper = new ObjectMapper();
        Config config = mapper.readValue(data, Config.class);
        return config;
    }
    public void consume(String port) throws IOException {

        Config config = null;
        try {
            config = getConfig();
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        startConnection(Integer.parseInt(port));
        Channel channel = nodes.get(port);

        DeliverCallback deliverCallback = (consumerTag, delivery) -> {
            String message = new String(delivery.getBody(), "UTF-8");
            System.out.println(" [x] Received '" + message + "'");
            MESSAGES.add(new Message(message));
        };

        channel.basicConsume("queue."+config.getUser(),
                true, deliverCallback, consumerTag -> {
                });
    }
    public void removeQueue(String port) throws IOException {
        startConnection(Integer.parseInt(port));
        Channel channel = nodes.get(port);
        String queueName = "queue." + getConfig().getUser();

        channel.queueDelete(queueName);
    }
}
