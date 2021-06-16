package com.edcs.controller;

import com.edcs.service.RabbitmqService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class chatController {

    @Autowired
    RabbitmqService rabbitmqService;

    @GetMapping("/chat")
    String show() throws Exception {
        return "chat.html";
    }
    @RequestMapping("/sendMessage")
    void sendMessage(@RequestParam(value = "MessageContent", required = false) String content) throws Exception {
        rabbitmqService.startConnection();
        System.out.println(content);
        rabbitmqService.sendMessage("First chat",content);
    }

    @GetMapping("/getMessage")
    void getMessage() throws Exception {
        rabbitmqService.getMessage();
    }

}
