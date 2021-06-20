package com.edcs.controller;

import com.edcs.model.ChatForm;
import com.edcs.model.Config;
import com.edcs.model.Message;
import com.edcs.model.connections.Chats;
import com.edcs.model.connections.Connections;
import com.edcs.model.connections.Users;
import com.edcs.service.RabbitmqService;
import com.edcs.utils.FileUtils;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.sun.org.apache.xpath.internal.operations.Mod;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;

@Controller
public class ChatController {

    @Autowired
    RabbitmqService rabbitmqService;

    @GetMapping("/createChat")
    public String submitChat(Model model) throws IOException {
        String port = rabbitmqService.getFreeNodePort();
        if (port == null) {
            model.addAttribute("submitResult", "There are not free nodes!");
        } else {
            rabbitmqService.createQueue(port);
            addChatPortToConnections(port);
            model.addAttribute("submitResult", "Chat created on port: " + port);
        }
        return "home.html";
    }
    @GetMapping("/")
    public String home() throws IOException {
        return "home.html";
    }

    @GetMapping("/joinChat")
    public String joinChat(Model model) throws IOException {
        model.addAttribute("chatform", new ChatForm());
        return "join_chat.html";
    }

    @RequestMapping(value = "/joinChat/submit", method=RequestMethod.POST)
    public String joinChatSubmit(@ModelAttribute(value="chatform")ChatForm chatForm, Model model) throws IOException {
        String chat = String.valueOf(Integer.parseInt(chatForm.getPort())-10);
        if(!rabbitmqService.ifUsed(chat)){
            rabbitmqService.createQueue(chatForm.getPort());
            addChatPortToConnections(chatForm.getPort());
            model.addAttribute("submitResult", "Chat joined on port: " + chatForm.getPort());
        }else
            model.addAttribute("submitResult", "Chat doesn't exist on port: " + chatForm.getPort());

        return "home.html";
    }

    private void addChatPortToConnections(String port) throws JsonProcessingException {
        String file = FileUtils.getConnectionsFile();
        if(!file.contains(port)) {
            ObjectMapper mapper = new ObjectMapper();
            mapper.enable(SerializationFeature.INDENT_OUTPUT);

            ObjectNode fileJson = (ObjectNode) mapper.readTree(file);
            ArrayNode users = (ArrayNode) fileJson.get("users");
            ArrayNode chats = (ArrayNode) users.get(0).get("chats");

            ObjectNode newChat = mapper.createObjectNode();
            newChat.put("port", port);
            chats.add(newChat);

            ObjectWriter writer = mapper.writer(new DefaultPrettyPrinter());
            try {
                File dir = new File("connections.json");
                writer.writeValue(dir, fileJson);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }
    private void removeChatPortFromConnections(String port) throws JsonProcessingException {
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

        ObjectWriter writer = mapper.writer(new DefaultPrettyPrinter());
        try {
            File dir = new File("connections.json");
            writer.writeValue(dir, fileJson);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @GetMapping("/chats")
    public String viewChats(Model model) throws JsonProcessingException {
        String data = FileUtils.getConfigFile();

        ObjectMapper mapper = new ObjectMapper();
        Config config = mapper.readValue(data, Config.class);
        String loggedUser = config.getUser();

        String connectionsData = FileUtils.getConnectionsFile();
        Connections connections = mapper.readValue(connectionsData, Connections.class);
        Chats[] chats = null;
        for (Users users : connections.getUsers()) {
            if (users.getUser().equals(loggedUser)) ;
            chats = users.getChats();
        }
        model.addAttribute("chats", chats);
        return "chats.html";
    }
//    @GetMapping("/bindTest")
//    public String bind() {
//        rabbitmqService.bindQueue("5672");
//        return "home.html";
//    }

    @RequestMapping(value = "/connectChat/{port}/sendMessage", method = RequestMethod.GET)
    String sendMessage(@ModelAttribute(value="message") Message message
            , @PathVariable String port,Model model) throws Exception {
        rabbitmqService.sendMessage(port, message.getContent());
        model.addAttribute("port",port);
        return "chat.html";
    }

    @GetMapping(value = "/connectChat/{port}")
    String getMessage(@PathVariable("port") String port,Model model) throws Exception {
        Message message = new Message();
        rabbitmqService.createQueue(port);
        rabbitmqService.consume(port);
        model.addAttribute("message", message);
        model.addAttribute("port",port);
        return "chat.html";
    }

    @GetMapping("/connectChat/{port}/refresh")
    String getMessageRefresh(@PathVariable String port,Model model) throws Exception {
        String result = rabbitmqService.consume(port);
        ArrayList<Message> messages = (ArrayList<Message>) rabbitmqService.MESSAGES.clone();
        rabbitmqService.MESSAGES.clear();
        Message message = new Message();
        model.addAttribute("message", message);
        model.addAttribute("messages",messages);
        model.addAttribute("port",port);
        model.addAttribute("result",result);
        return "chat.html";
    }

    @GetMapping("/connectChat/{port}/leave")
    String leaveChat(@PathVariable String port,Model model) throws Exception {
        rabbitmqService.removeQueue(port);
        removeChatPortFromConnections(port);
        model.addAttribute("submitResult", "Chat "+port+" has been left by this user!");
        return "home.html";
    }
}
