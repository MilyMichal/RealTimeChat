package com.m.m.RealTimeChat.Controllers;

import com.m.m.RealTimeChat.Models.OnlineUser;
import com.m.m.RealTimeChat.Services.OnlineUserService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class OnlineUsersController {

    private final OnlineUserService onlineUserService;

    public OnlineUsersController(OnlineUserService onlineUserService) {
        this.onlineUserService = onlineUserService;
    }

    @GetMapping("/users")
    public List<OnlineUser> getOnlineUsers() {
        return onlineUserService.getAllOnlineUsers();
    }
}
