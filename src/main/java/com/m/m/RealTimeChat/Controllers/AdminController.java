package com.m.m.RealTimeChat.Controllers;

import com.m.m.RealTimeChat.Services.UserStorageService;
import com.sun.net.httpserver.Headers;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.Header;
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

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PutMapping("/banned/{userName}")
    public void banUser(@PathVariable String userName) {
        LocalDateTime banExp = LocalDateTime.now().plusMinutes(1);
        System.out.println("RESTCONTROLLER DEBUG METHOD");
        userStorageService.banUser(userName, banExp);
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PutMapping("/unban/{userName}")
    public void unBanUser(@PathVariable String userName) {
        System.out.println("RESTCONTROLLER UNBAN DEBUG METHOD");
        userStorageService.unBanUser(userName);
    }


   /* @GetMapping("/data/{user}")
    public ResponseEntity<byte[]> getProfilePicture(@PathVariable String user) {
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_TYPE, "image/jpeg");
        byte[] profPic = userStorageService.getProfilePic(user);
        return new ResponseEntity<>(profPic, headers, HttpStatus.OK);
    }*/
}
