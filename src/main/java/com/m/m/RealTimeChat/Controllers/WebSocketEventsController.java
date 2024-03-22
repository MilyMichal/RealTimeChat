package com.m.m.RealTimeChat.Controllers;

import com.m.m.RealTimeChat.Models.Message;
import com.m.m.RealTimeChat.Models.OnlineUserStorage;
import com.m.m.RealTimeChat.Services.MessageHistoryService;
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

    public WebSocketEventsController(OnlineUserService onlineUserService, MessageHistoryService messageHistoryService) {
        this.onlineUserService = onlineUserService;
    }

    @EventListener
    public void disconnect(SessionDisconnectEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());

        String username = (String) headerAccessor.getSessionAttributes().get("sender");
        onlineUserService.removeOnlineUser(username);
        /*Message disconnectMsg = new Message();
        disconnectMsg.setContent(username + " just left the chatroom");
        disconnectMsg.setType("Leave");
        disconnectMsg.setSender(username);
        disconnectMsg.setSendTo("public");*/

        Map<String,String> disconnectedUser = new HashMap<>();
        disconnectedUser.put("type","logout");
        disconnectedUser.put("sender",username);
        //new feature
        disconnectedUser.put("content",username + " just left the chatroom");
        //
        messagingTemplate.convertAndSend("/topic/chat", disconnectedUser);
        System.out.println("Disconnect event: " + username);

    }

}

