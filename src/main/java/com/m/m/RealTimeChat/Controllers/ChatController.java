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
        model.addAttribute("user", userStorageService.getUser(authentication.getName()).getNickname());
        model.addAttribute("usersList", userStorageService.getOtherNicknamesList(authentication));
        model.addAttribute("serverURL", serverURL);

        if (authentication.getAuthorities().stream().anyMatch(role -> role.getAuthority().equalsIgnoreCase("role_admin"))) {
            model.addAttribute("users", onlineUserService.getAllOnlineUsers());
            model.addAttribute("bannedUsers", userStorageService.getBannedNicknames());
            return "chat-admin";
        }
        return "ChatPage";
    }

  /*  @GetMapping("/oauth2/google")
    public String openChatWithGoogleAuth (OAuth2AuthenticationToken authenticationToken,Model model) {
        model.addAttribute("user", reformatUsername(Objects.requireNonNull(authenticationToken.getPrincipal().getAttribute("given_name"))));
        model.addAttribute("user", userStorageService.getUser(authenticationToken.getName()).getNickname());
        model.addAttribute("usersList", userStorageService.getAllNicknames());
        model.addAttribute("photo",authenticationToken.getPrincipal().getAttribute("picture"));
        System.out.println("PHOTO DEBUG: " + authenticationToken.getPrincipal().getAttribute("picture"));
        model.addAttribute("serverURL", serverURL);

        return "ChatPage";

    }

    private String reformatUsername(String username) {
        return username.replaceAll(" ","_");
    }*/

}
