package com.m.m.RealTimeChat.Controllers;

import com.m.m.RealTimeChat.Services.UserStorageService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/admin")
public class AdminController {

    private final UserStorageService userStorageService;

    public AdminController(UserStorageService userStorageService) {
        this.userStorageService = userStorageService;
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/banned/{userName}-{banDuration}")
    public void banUser(@PathVariable String userName,@PathVariable int banDuration) {
        LocalDateTime banExp = LocalDateTime.now().plusMinutes(banDuration);

        userStorageService.banUser(userName, banExp);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/unban/{userName}")
    public void unBanUser(@PathVariable String userName) {

        userStorageService.unBanUser(userName);
    }

}
