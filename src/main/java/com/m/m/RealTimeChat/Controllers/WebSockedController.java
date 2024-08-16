package com.m.m.RealTimeChat.Controllers;

import com.m.m.RealTimeChat.Models.Message;


import com.m.m.RealTimeChat.Services.MessageHistoryService;
import com.m.m.RealTimeChat.Services.OnlineUserService;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.stereotype.Controller;

import java.util.Objects;


@Controller
public class WebSockedController {


    private final MessageHistoryService messageHistoryService;

    private final OnlineUserService onlineUserService;

    public WebSockedController(MessageHistoryService messageHistoryService, OnlineUserService onlineUserService) {
        this.messageHistoryService = messageHistoryService;
        this.onlineUserService = onlineUserService;
    }

    @MessageMapping("/chat")
    @SendTo("/topic/chat")
    public Message sendMsg(@Payload Message msg) {
        /*System.out.println("RECEIVED MSG DEBUG:" + msg);*/
        messageHistoryService.saveMessage(msg);
        /*System.out.println("MESSAGE SAVING DEBUG: sendMsg method");*/
        return msg;
    }


    @MessageMapping("/user")
    @SendTo("/topic/chat")
    public Message newUser(@Payload Message message, SimpMessageHeaderAccessor headerAccessor) {
        /*System.out.println("DEBUG USERS SESSION ID: " + headerAccessor.getSessionId());*/
        Objects.requireNonNull(headerAccessor.getSessionAttributes()).put("sender", message.getSender());
        messageHistoryService.saveMessage(message);
        if (onlineUserService.findOnlineUser(message.getSender()).isEmpty()) {
            onlineUserService.addOnlineUser(message.getSender());
        }
        /*System.out.println("MESSAGE SAVING DEBUG: newUser method");*/
        return message;

    }

}
