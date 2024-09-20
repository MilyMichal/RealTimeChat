package com.m.m.RealTimeChat.Controllers;

import com.m.m.RealTimeChat.Services.OnlineUserService;
import com.m.m.RealTimeChat.Services.UserStorageService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;


@Controller
@RequestMapping("/chat")
public class ChatController {

    @Value("${spring.datasource.serverURL}")
    private String serverURL;

    private final OnlineUserService onlineUserService;

    private final UserStorageService userStorageService;

    public ChatController(OnlineUserService onlineUserService, UserStorageService userStorageService) {
        this.onlineUserService = onlineUserService;

        this.userStorageService = userStorageService;
    }

    @GetMapping
    public String openChat(Model model, Authentication authentication) {
        model.addAttribute("user", userStorageService.getUser(authentication.getName()).getNickname()); // zd evložit nickname míst username!!
        model.addAttribute("usersList",userStorageService.getNicknameList(authentication.getName()));
        model.addAttribute("serverURL",serverURL);
        if (authentication.getAuthorities().stream().anyMatch(role -> role.getAuthority().equalsIgnoreCase("admin"))) {
            model.addAttribute("users", onlineUserService.getAllOnlineUsers());
            model.addAttribute("bannedUsers",userStorageService.getBannedUsers());
            return "chat-admin";
        }
        return "ChatPage";
    }



}
