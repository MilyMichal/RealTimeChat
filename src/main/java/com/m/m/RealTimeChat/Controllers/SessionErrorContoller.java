package com.m.m.RealTimeChat.Controllers;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;


@Controller
@RequestMapping("/sessionError")
public class SessionErrorContoller {

    @GetMapping
    public String showSessionError(){
         return "session";
    }


}