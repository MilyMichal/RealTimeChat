package com.m.m.RealTimeChat.Controllers;

import com.m.m.RealTimeChat.Models.Message;
import com.m.m.RealTimeChat.Models.MessageHisrotyStorage;

import com.m.m.RealTimeChat.Models.OnlineUserStorage;
import com.m.m.RealTimeChat.Services.MessageHistoryService;
import com.m.m.RealTimeChat.Services.UserStorageService;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.stereotype.Controller;

import java.util.List;


@Controller
public class WebSockedController {

    //
    private final MessageHistoryService messageHistoryService;
    private final UserStorageService userStorageService;

    //
    private final OnlineUserStorage userStorage;
    private final MessageHisrotyStorage storage;

    public WebSockedController(MessageHistoryService messageHistoryService, UserStorageService userStorageService, OnlineUserStorage userStorage, MessageHisrotyStorage storage) {
        this.messageHistoryService = messageHistoryService;
        this.userStorageService = userStorageService;
        this.userStorage = userStorage;
        this.storage = storage;
    }

    @MessageMapping("/chat")
    @SendTo("/topic/chat")
    public Message sendMsg(Message msg) {
        //new feature
        //
        messageHistoryService.saveMessage(msg);
        //

        storage.saveMessage(msg);
        return msg;
    }


    @MessageMapping("/user")
    @SendTo("/topic/chat")
    public List<String> newUser(@Payload Message message, SimpMessageHeaderAccessor headerAccessor) {
        headerAccessor.getSessionAttributes().put("sender", message.getSender());

        //userStorageService.saveUserToStorage();
        userStorage.getOnlineUsers().add(message.getSender());
        return userStorage.getOnlineUsers();
    }

}

