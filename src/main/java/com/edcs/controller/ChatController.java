package com.edcs.controller;

import com.edcs.model.Config;
import com.edcs.model.connections.Chats;
import com.edcs.model.connections.Connections;
import com.edcs.model.connections.Users;
import com.edcs.service.RabbitmqService;
import com.edcs.utils.FileUtils;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;

@Controller
public class ChatController {

    @Autowired
    RabbitmqService rabbitmqService;

    @GetMapping("/createChat")
    public String submitChat(Model model) throws IOException {
        String port = rabbitmqService.getFreeNodePort();
        if(port==null){
            model.addAttribute("submitResult", "There are not free nodes!");
        }
        else {
            addChatPortToConnections(port);
            rabbitmqService.createQueue(port);
            model.addAttribute("submitResult", "Chat created on port: " + port);
        }
        return "home.html";
    }

    private void addChatPortToConnections(String port) throws JsonProcessingException {
        String file = FileUtils.getConnectionsFile();
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

    @GetMapping("/chats")
    public String viewChats(Model model) throws JsonProcessingException {
        String data = FileUtils.getConfigFile();

        ObjectMapper mapper = new ObjectMapper();
        Config config = mapper.readValue(data, Config.class);
        String loggedUser = config.getUser();

        String connectionsData = FileUtils.getConnectionsFile();
        Connections connections = mapper.readValue(connectionsData, Connections.class);
        Chats[] chats = null;
        for(Users users:connections.getUsers()){
            if(users.getUser().equals(loggedUser));
                chats = users.getChats();
        }
        model.addAttribute("chats", chats);
        return "chats.html";
    }
    @GetMapping("/bindTest")
    public String bind() {
        rabbitmqService.bindQueue("5672");
        return "home.html";
    }

    @GetMapping("/chat")
    String show() throws Exception {
        return "chat.html";
    }

    @RequestMapping(value="/chat/sendMessage", method = RequestMethod.GET)
    void sendMessage(@RequestParam String content
            , @RequestParam String port) throws Exception {
        rabbitmqService.sendMessage(port, "Chat", content);
    }

    @GetMapping(value = "/connectChat/{port}")
    String enterFirstChat(@PathVariable("port") String port) throws Exception {
        rabbitmqService.startConnection(Integer.parseInt(port));
        return "chat.html";
    }

    @RequestMapping("/subscribe")
    void subscribe(@RequestParam(value = "messageContent", required = false) String content) throws Exception {
        //   rabbitmqService.startConnection();
    }
//    @GetMapping("/getMessage")
//    void getMessage() throws Exception {
//        rabbitmqService.getMessage();
//    }

}
