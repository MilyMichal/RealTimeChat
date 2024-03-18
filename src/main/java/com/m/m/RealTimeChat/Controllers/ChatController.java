package com.m.m.RealTimeChat.Controllers;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/chat")
public class ChatController {

    @GetMapping
    public String openChat(Model model, HttpServletRequest request) {
        model.addAttribute("user",request.getUserPrincipal().getName());
        return "ChatPage";
    }
}
