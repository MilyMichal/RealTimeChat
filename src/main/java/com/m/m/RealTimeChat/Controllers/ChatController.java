package com.m.m.RealTimeChat.Controllers;

import com.m.m.RealTimeChat.Services.OnlineUserService;
import com.m.m.RealTimeChat.Services.UserStorageService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;


@Controller
@RequestMapping("/chat")
public class ChatController {

    private final OnlineUserService onlineUserService;

    private final UserStorageService userStorageService;

    public ChatController(OnlineUserService onlineUserService, UserStorageService userStorageService) {
        this.onlineUserService = onlineUserService;

        this.userStorageService = userStorageService;
    }

    @GetMapping
    public String openChat(Model model, HttpServletRequest request, Authentication authentication) {
        model.addAttribute("user", authentication.getName());
        if (authentication.getAuthorities().stream().anyMatch(role -> role.getAuthority().equalsIgnoreCase("admin"))) {
            model.addAttribute("users", onlineUserService.getAllOnlineUsers());
            model.addAttribute("bannedUsers",userStorageService.getBannedUsers());
            return "chat-admin";
        }
        model.addAttribute("usersList",userStorageService.getUsersList(authentication.getName()));
        return "ChatPage";
    }



}
