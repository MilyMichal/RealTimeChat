package com.m.m.real.time.chat.controllers;

import com.m.m.real.time.chat.services.OnlineUserService;
import com.m.m.real.time.chat.services.UserStorageService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
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
        model.addAttribute("user", userStorageService.getUser(authentication.getName()).getNickname());
        model.addAttribute("usersList", userStorageService.getOtherNicknamesList(authentication));
        model.addAttribute("serverURL", serverURL);
        model.addAttribute("Oauth",authentication instanceof OAuth2AuthenticationToken );
        if (authentication.getAuthorities().stream().anyMatch(role -> role.getAuthority().equalsIgnoreCase("role_admin"))) {
            model.addAttribute("users", onlineUserService.getAllOnlineUsers());
            model.addAttribute("bannedUsers", userStorageService.getBannedNicknames());
            return "chatAdmin";
        }
        return "chatPage";
    }

 }
