package com.m.m.RealTimeChat.Controllers;

import com.m.m.RealTimeChat.Models.OnlineUserStorage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import java.util.HashMap;
import java.util.Map;


@Component
public class WebSocketEventsController {

    private final OnlineUserStorage storage;

    @Autowired
    private SimpMessageSendingOperations messagingTemplate;

    public WebSocketEventsController(OnlineUserStorage storage) {
        this.storage = storage;
    }

    @EventListener
    public void disconect(SessionDisconnectEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());

        String username = (String) headerAccessor.getSessionAttributes().get("sender");
        storage.onlineUsers.remove(username);
        Map<String,String> disconnectedUser = new HashMap<>();
        disconnectedUser.put("type","Leave");
        disconnectedUser.put("user",username);
        messagingTemplate.convertAndSend("/topic/chat", disconnectedUser);
        System.out.println("Disconnect event: " + username);

    }

}

