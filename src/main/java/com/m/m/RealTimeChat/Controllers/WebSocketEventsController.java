package com.m.m.RealTimeChat.Controllers;

import com.m.m.RealTimeChat.Models.OnlineUserStorage;
import com.m.m.RealTimeChat.Services.OnlineUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import java.time.LocalDate;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;


@Component
public class WebSocketEventsController {


    private final OnlineUserService onlineUserService;

    @Autowired
    private SimpMessageSendingOperations messagingTemplate;

    public WebSocketEventsController( OnlineUserService onlineUserService) {
        this.onlineUserService = onlineUserService;
    }

    @EventListener
    public void disconnect(SessionDisconnectEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());

        String username = (String) headerAccessor.getSessionAttributes().get("sender");
        onlineUserService.removeOnlineUser(username);
        Map<String,String> disconnectedUser = new HashMap<>();
        disconnectedUser.put("type","Leave");
        disconnectedUser.put("user",username);
        //new feature
        disconnectedUser.put("content",username + " just left the chatroom");
        //
        messagingTemplate.convertAndSend("/topic/chat", disconnectedUser);
        System.out.println("Disconnect event: " + username);

    }

}

