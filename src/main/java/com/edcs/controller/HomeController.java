package com.edcs.controller;

import com.edcs.service.RabbitmqService;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    @Autowired
    RabbitmqService rabbitmqService;


//    @GetMapping("/createChat/submit")
//    public String createChat() throws JsonProcessingException {
//        rabbitmqService.getFreeNodePort();
//        return "home.html";
//    }
    @GetMapping("/home")
    public String home()
    {
        return "home.html";
    }

}