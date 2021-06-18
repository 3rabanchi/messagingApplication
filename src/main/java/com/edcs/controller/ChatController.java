package com.edcs.controller;

import com.edcs.service.RabbitmqService;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class ChatController {

    @Autowired
    RabbitmqService rabbitmqService;

//    @GetMapping("/createChat")
//    public String redirectChatCreation() {
//        return "create_chat.html";
//    }

    @GetMapping("/createChat")
    public String submitChat(Model model) throws JsonProcessingException {
        String port = rabbitmqService.getFreeNodePort();
        if(port==null){
            model.addAttribute("submitResult", "There are not free nodes!");
        }
        else
            model.addAttribute("submitResult", "Chat created on port: "+ port);
        return "home.html";
    }

    @GetMapping("/chat")
    String show() throws Exception {
        return "chat.html";
    }

    @GetMapping(value = "/firstChat/sendMessage/{content}")
    void sendMessage(@PathVariable("content") String content) throws Exception {
        rabbitmqService.sendMessage("5672", "First chat", content);
    }

    @GetMapping(value = "/secondChat/sendMessage/{content}")
    void sendMessageSecondChat(@PathVariable("content") String content) throws Exception {
        System.out.println(content);
        rabbitmqService.sendMessage("5673", "Second chat", content);
    }

    @GetMapping(value = "/thirdChat/sendMessage/{content}")
    void sendMessageThirdChat(@PathVariable("content") String content) throws Exception {
        System.out.println(content);
        rabbitmqService.sendMessage("5674", "Third chat", content);
    }

    @GetMapping(value = "/firstChat")
    String enterFirstChat() throws Exception {
        rabbitmqService.startConnection(5672);
        return "chat.html";
    }

    @GetMapping(value = "/secondChat")
    String sendSecondChat() throws Exception {
        rabbitmqService.startConnection(5673);
        return "chat.html";
    }

    @GetMapping(value = "/thirdChat")
    String enterThirdChat() throws Exception {
        rabbitmqService.startConnection(5674);
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
