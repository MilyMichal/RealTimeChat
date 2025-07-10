package com.m.m.real.time.chat.controllers;

import com.m.m.real.time.chat.services.UserStorageService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;

@RestController
@RequestMapping("/admin")
public class AdminController {

    private final UserStorageService userStorageService;

    public AdminController(UserStorageService userStorageService) {
        this.userStorageService = userStorageService;
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/banned")
    public void banUser(@RequestBody Map<String,String> banInfo) {
        LocalDateTime banExp = LocalDateTime.now().plusMinutes(Long.parseLong(banInfo.get("banDuration")));

        userStorageService.banUser(banInfo.get("nickname"), banExp);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/unban")
    public void unBanUser(@RequestBody Map<String,String> userInfo) {

        userStorageService.unBanUser(userInfo.get("nickname"));
    }

}
