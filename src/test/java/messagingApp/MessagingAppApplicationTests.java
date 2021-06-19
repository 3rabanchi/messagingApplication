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
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.Consumer;
import org.json.JSONException;
import org.junit.Assert;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.HashMap;

class MessagingAppApplicationTests {

    public HashMap<String,Channel> nodes = new HashMap<>();

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
        Channel channel = startConnection(5672);
        channel.queueDeclare("Test Queue", false, false, false, null);
        channel.queueBind("Test Queue","","");

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

}
