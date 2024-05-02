package com.m.m.RealTimeChat.Configuration;

import com.m.m.RealTimeChat.Services.UserStorageService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/admin")
@PreAuthorize("admin")
public class AdminController {

    private final UserStorageService userStorageService;

    public AdminController(UserStorageService userStorageService) {
        this.userStorageService = userStorageService;
    }

    @PostMapping("/banned/{user}")
    public void banUser(@PathVariable String user) {
        LocalDateTime banExp = LocalDateTime.now().plusMinutes(1);
        System.out.println("RESTCONTROLLER DEBUG METHOD");
        userStorageService.banUser(user, banExp);
    }

    @PostMapping("/unban/{user}")
    public void unBanUser(@PathVariable String user) {
        System.out.println("RESTCONTROLLER UNBAN DEBUG METHOD");
        userStorageService.unBanUser(user);
    }
}
