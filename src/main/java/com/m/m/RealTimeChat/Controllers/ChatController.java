package com.m.m.RealTimeChat.Controllers;

import com.m.m.RealTimeChat.Services.OnlineUserService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.net.http.HttpResponse;

@Controller
@RequestMapping("/chat")
public class ChatController {

    private final OnlineUserService  onlineUserService;

    public ChatController(OnlineUserService onlineUserService) {
        this.onlineUserService = onlineUserService;
    }

    @GetMapping
    public String openChat(Model model, HttpServletRequest request, Authentication authentication) {
        model.addAttribute("user",request.getUserPrincipal().getName());
        if (authentication.getAuthorities().stream().anyMatch(role -> role.getAuthority().equalsIgnoreCase("admin"))){
           model.addAttribute("users",onlineUserService.getAllOnlineUsers());
           return "chat-admin";
       }
        return "ChatPage";
    }
}
