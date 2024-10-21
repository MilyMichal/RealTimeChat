package com.m.m.RealTimeChat.Controllers;

import com.m.m.RealTimeChat.Models.Message;


import com.m.m.RealTimeChat.Models.MessageDTO;
import com.m.m.RealTimeChat.Services.MessageHistoryService;
import com.m.m.RealTimeChat.Services.MessageService;
import com.m.m.RealTimeChat.Services.OnlineUserService;
import com.m.m.RealTimeChat.Services.UserStorageService;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.security.Principal;
import java.util.Objects;


@Controller
public class WebSockedController {


    private final MessageHistoryService messageHistoryService;

    private final OnlineUserService onlineUserService;

    private final SimpMessagingTemplate simpMessagingTemplate;

    private final UserStorageService userStorageService;

    private final MessageService messageService;

    public WebSockedController(MessageHistoryService messageHistoryService, OnlineUserService onlineUserService, SimpMessagingTemplate simpMessagingTemplate, UserStorageService userStorageService, MessageService messageService) {
        this.messageHistoryService = messageHistoryService;
        this.onlineUserService = onlineUserService;
        this.simpMessagingTemplate = simpMessagingTemplate;
        this.userStorageService = userStorageService;
        this.messageService = messageService;
    }

    @MessageMapping("/chat/public")
    @SendTo("/queue/public")
    public Message sendMsg(@Payload MessageDTO msg, Principal principal) {
        Message message = messageService.generateMessage(msg, userStorageService.getUser(principal.getName()).getNickname());
        if (msg.getType().equals("kick") || msg.getType().equals("BAN") || msg.getType().equals("UNBAN")) {
            if(principal.getName().equals("Admin")) {
                messageHistoryService.saveMessage(message);
                return message;
            } else {
                return null;
            }
        }
        messageHistoryService.saveMessage(message);
        return message;
    }

    @MessageMapping("/chat/private")
    public void sendPrivateMessage(@Payload MessageDTO incomingMsg,Principal principal) {
        Message message = messageService.generateMessage(incomingMsg, userStorageService.getUser(principal.getName()).getNickname());
        messageHistoryService.saveMessage(message);

        String recipient = userStorageService.getUserByNickname(incomingMsg.getRecipient()).getUserName();
        String sender = principal.getName();/*userStorageService.getUserByNickname(incomingMsg.getSender()).getUserName();*/

        simpMessagingTemplate.convertAndSendToUser(recipient, "/queue/private", message);
        simpMessagingTemplate.convertAndSendToUser(sender, "/queue/private", message);
    }


    @MessageMapping("/user")
    @SendTo("/queue/public")
    public Message newUser(@Payload MessageDTO incomingMsg,Principal principal, SimpMessageHeaderAccessor headerAccessor) {
        Message message = messageService.generateMessage(incomingMsg, userStorageService.getUser(principal.getName()).getNickname());
        Objects.requireNonNull(headerAccessor.getSessionAttributes()).put("sender", message.getSender());
        messageHistoryService.saveMessage(message);
        if (onlineUserService.findOnlineUser(message.getSender()).isEmpty()) {
            onlineUserService.addOnlineUser(message.getSender());
        }
        return message;

    }

}
