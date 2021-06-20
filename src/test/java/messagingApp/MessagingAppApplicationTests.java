package messagingApp;

import com.edcs.model.Config;
import com.edcs.model.Nodes;
import com.edcs.service.RabbitmqService;
import com.edcs.utils.FileUtils;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.microsoft.signalr.HubConnection;
import com.microsoft.signalr.HubConnectionBuilder;
import com.rabbitmq.client.*;
import org.json.JSONException;
import org.junit.Assert;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;

class MessagingAppApplicationTests {

    public HashMap<String, Channel> nodes = new HashMap<>();
    public static final String EXCHANGE_NAME = "Messaging";

    @Test
    void checkMethodIfUsed() {
        RabbitmqService rabbitmqService = new RabbitmqService();
        Assert.assertFalse(rabbitmqService.ifUsed("15672"));
    }

    @Test
    void checkConfigFile() throws IOException, JSONException {
        String data = FileUtils.getConfigFile();

        ObjectMapper mapper = new ObjectMapper();
        Config config = mapper.readValue(data, Config.class);
        System.out.println(config.getNodes()[1].getManagementPort());
        //	System.out.println(level);
    }

    @Test
    public void getFreeNodeTest() throws JsonProcessingException {
        String data = FileUtils.getConfigFile();
        RabbitmqService rabbitmqService = new RabbitmqService();

        ObjectMapper mapper = new ObjectMapper();
        Config config = mapper.readValue(data, Config.class);
        for (Nodes node : config.getNodes()) {
            System.out.println(node.getManagementPort() + " - checking!");
            if (rabbitmqService.ifUsed(node.getManagementPort()))
                System.out.println(node.getManagementPort() + " is not used!");
            else
                System.out.println(node.getManagementPort() + " is used!");
        }
        //;	Assert.assertTrue(true);
        //	return config.getNodes()[1].getAmqpPort();
    }

    @Test
    public void bindingTest() throws Exception {
//        Channel channel = startConnection(5672);
//        channel.queueDeclare("Test Queue", false, false, false, null);
//        channel.queueBind("Test Queue","","");

    }

    @Test
    public void removeChatNodeConfig() throws JsonProcessingException {
        String port = "1011";
        String file = FileUtils.getConnectionsFile();
        ObjectMapper mapper = new ObjectMapper();
        mapper.enable(SerializationFeature.INDENT_OUTPUT);

        ObjectNode fileJson = (ObjectNode) mapper.readTree(file);
        ArrayNode ports = (ArrayNode) fileJson.get("users").get(0).get("chats");
        JsonNode wantedPort = null;
        int iterator = 0;
        for (JsonNode currentPort : ports) {
            if (currentPort.get("port").toString().contains(port)) {
                wantedPort = currentPort;
                break;
            }
            iterator++;
        }
        if (wantedPort != null)
            ports.remove(iterator);
        System.out.println(mapper.writeValueAsString(fileJson));
    }

    @Test
    public void addChatNodeConfig() throws JsonProcessingException {
        String port = "1111";
        String file = FileUtils.getConnectionsFile();
        ObjectMapper mapper = new ObjectMapper();
        mapper.enable(SerializationFeature.INDENT_OUTPUT);

        ObjectNode fileJson = (ObjectNode) mapper.readTree(file);
        ArrayNode users = (ArrayNode) fileJson.get("users");
        ArrayNode chats = (ArrayNode) users.get(0).get("chats");

        ObjectNode newChat = mapper.createObjectNode();
        newChat.put("port", port);
        chats.add(newChat);

        System.out.println(mapper.writeValueAsString(fileJson));

    }

    @Test
    public void sendMessages() throws Exception {
        startConnection(1011);
        Channel channel = nodes.get("1011");
        channel.basicPublish(EXCHANGE_NAME, "user.dupa", null, "brumba".getBytes());
    }

    @Test
    public void consumeMessage() throws Exception {
        final HubConnection hubConnection = HubConnectionBuilder.create("/hub").build();
        Config config = getConfig();
        startConnection(1011);
        Channel channel = nodes.get("1011");

        DeliverCallback deliverCallback = (consumerTag, delivery) -> {
            String message = new String(delivery.getBody(), "UTF-8");
            System.out.println(" [x] Received '" + message + "'");
            hubConnection.send("send", message);
        };

        channel.basicConsume("queue." + config.getUser(),
                true, deliverCallback, consumerTag -> {
                });
    }

    public void startConnection(int port) throws Exception {
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
        if (port > 1013)
            factory.setHost("192.168.8.156");
        else
            factory.setHost("192.168.8.150");
    }

    private Config getConfig() throws JsonProcessingException {
        String data = FileUtils.getConfigFile();

        ObjectMapper mapper = new ObjectMapper();
        Config config = mapper.readValue(data, Config.class);
        return config;
    }

}
