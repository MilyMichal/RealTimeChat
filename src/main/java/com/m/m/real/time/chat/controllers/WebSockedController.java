package com.m.m.real.time.chat.controllers;

import com.m.m.real.time.chat.configuration.RateLimitConfiguration;
import com.m.m.real.time.chat.errors.RateLimitExceededException;
import com.m.m.real.time.chat.models.Message;


import com.m.m.real.time.chat.models.MessageDTO;
import com.m.m.real.time.chat.services.MessageHistoryService;
import com.m.m.real.time.chat.services.MessageService;
import com.m.m.real.time.chat.services.OnlineUserService;
import com.m.m.real.time.chat.services.UserStorageService;
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
    private final RateLimitConfiguration rateLimitConfiguration;

    public WebSockedController(MessageHistoryService messageHistoryService, OnlineUserService onlineUserService, SimpMessagingTemplate simpMessagingTemplate, UserStorageService userStorageService, MessageService messageService, RateLimitConfiguration rateLimitConfiguration) {

        this.messageHistoryService = messageHistoryService;
        this.onlineUserService = onlineUserService;
        this.simpMessagingTemplate = simpMessagingTemplate;
        this.userStorageService = userStorageService;
        this.messageService = messageService;
        this.rateLimitConfiguration = rateLimitConfiguration;
    }

    @MessageMapping("/chat/public")
    @SendTo("/queue/public")
    public Message sendMsg(@Payload MessageDTO msg, Principal principal) {
        if (!rateLimitConfiguration.isRateLimitExceeded(principal)) {
            Message message = messageService.generateMessage(msg, userStorageService.getUser(principal.getName()).getNickname());
            if (isAuthorized(principal, msg.getType())) {
                messageHistoryService.saveMessage(message);
                return message;
            } else {
                return null;
            }
        } else {
            throw new RateLimitExceededException("Too many messages in short time! \n Try again after few seconds.");

        }
    }

    @MessageMapping("/chat/private")
    public void sendPrivateMessage(@Payload MessageDTO incomingMsg, Principal principal) {
        if (!rateLimitConfiguration.isRateLimitExceeded(principal)) {
       Message message = messageService.generateMessage(incomingMsg, userStorageService.getUser(principal.getName()).getNickname());
        if (isAuthorized(principal, incomingMsg.getType())) {
            messageHistoryService.saveMessage(message);
            String recipient = userStorageService.getUserByNickname(incomingMsg.getRecipient()).getUserName();
            simpMessagingTemplate.convertAndSendToUser(recipient, "/queue/private", message);
            simpMessagingTemplate.convertAndSendToUser(principal.getName(), "/queue/private", message);

        }
        } else {
            throw new RateLimitExceededException("Too many messages in short time! \n Try again after few seconds.");
        }
    }


    @MessageMapping("/user")
    @SendTo("/queue/public")
    public Message newUser(@Payload MessageDTO incomingMsg, Principal principal, SimpMessageHeaderAccessor headerAccessor) {
        Message message = messageService.generateMessage(incomingMsg, userStorageService.getUser(principal.getName()).getNickname());
        Objects.requireNonNull(headerAccessor.getSessionAttributes()).put("sender", message.getSender());
        messageHistoryService.saveMessage(message);
        if (onlineUserService.findOnlineUser(message.getSender()).isEmpty()) {
            onlineUserService.addOnlineUser(message.getSender());
        }
        return message;

    }

    private boolean isAuthorized(Principal sender, String msgType) {
        if (msgType.equalsIgnoreCase("kick") || msgType.equalsIgnoreCase("BAN") || msgType.equalsIgnoreCase("UNBAN")) {
            return userStorageService.getUser(sender.getName()).getRoles().equalsIgnoreCase("Admin");
        }
        return true;
    }
}

