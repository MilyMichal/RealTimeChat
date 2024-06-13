package com.m.m.RealTimeChat.Controllers;



import com.m.m.RealTimeChat.Services.OnlineUserService;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;

import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.Map;


@RestController
@ResponseStatus(HttpStatus.OK)
public class SessionErrorContoller {

    private final OnlineUserService onlineUserService;

    public SessionErrorContoller(OnlineUserService onlineUserService) {
        this.onlineUserService = onlineUserService;
    }

    @PutMapping("/session-expired")
    @ResponseStatus(HttpStatus.OK)
    public void sessionExpired(HttpServletResponse response, @RequestBody Map<String, String> data) throws IOException {

        System.out.println("EXPIRED SESSION DEBUG: SESSION IS EXPIRED! ");
        onlineUserService.removeOnlineUser(data.get("expiredUser"));


    }
}


