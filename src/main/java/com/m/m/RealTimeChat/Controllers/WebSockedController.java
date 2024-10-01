package com.m.m.RealTimeChat.Controllers;

import com.m.m.RealTimeChat.Models.Message;


import com.m.m.RealTimeChat.Services.MessageHistoryService;
import com.m.m.RealTimeChat.Services.OnlineUserService;
import com.m.m.RealTimeChat.Services.UserStorageService;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.stereotype.Controller;

import java.util.Objects;


@Controller
public class WebSockedController {


    private final MessageHistoryService messageHistoryService;

    private final OnlineUserService onlineUserService;

    private final SimpMessagingTemplate simpMessagingTemplate;

    private final UserStorageService userStorageService;

    public WebSockedController(MessageHistoryService messageHistoryService, OnlineUserService onlineUserService, SimpMessagingTemplate simpMessagingTemplate, UserStorageService userStorageService) {
        this.messageHistoryService = messageHistoryService;
        this.onlineUserService = onlineUserService;
        this.simpMessagingTemplate = simpMessagingTemplate;
        this.userStorageService = userStorageService;
    }

    @MessageMapping("/chat/public")
    @SendTo("/queue/public")
    public Message sendMsg(@Payload Message msg) {
        /*System.out.println("RECEIVED MSG DEBUG:" + msg);*/
        messageHistoryService.saveMessage(msg);
        /*System.out.println("MESSAGE SAVING DEBUG: sendMsg method");*/
        return msg;
    }

    @MessageMapping("/chat/private")
    public void sendPrivateMessage(@Payload Message msg) {
        messageHistoryService.saveMessage(msg);

    String recipient = userStorageService.getUserByNickname(msg.getSendTo()).getUserName();
    String sender = userStorageService.getUserByNickname(msg.getSender()).getUserName();

        simpMessagingTemplate.convertAndSendToUser(recipient,"/queue/private",msg);
        simpMessagingTemplate.convertAndSendToUser(sender,"/queue/private",msg);
    }


    @MessageMapping("/user")
    @SendTo("/queue/public")
    public Message newUser(@Payload Message message, SimpMessageHeaderAccessor headerAccessor) {

        Objects.requireNonNull(headerAccessor.getSessionAttributes()).put("sender", message.getSender());
        messageHistoryService.saveMessage(message);
        if (onlineUserService.findOnlineUser(message.getSender()).isEmpty()) {
            onlineUserService.addOnlineUser(message.getSender());
        }
        /*System.out.println("MESSAGE SAVING DEBUG: newUser method");*/
        return message;

    }

}
