package com.m.m.RealTimeChat.Controllers;

import com.m.m.RealTimeChat.Models.Message;

import com.m.m.RealTimeChat.Models.OnlineUser;
import com.m.m.RealTimeChat.Models.OnlineUserStorage;
import com.m.m.RealTimeChat.Services.MessageHistoryService;
import com.m.m.RealTimeChat.Services.OnlineUserService;
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


    //

    private final OnlineUserService onlineUserService;

    public WebSockedController(MessageHistoryService messageHistoryService,OnlineUserService onlineUserService) {
        this.messageHistoryService = messageHistoryService;


        this.onlineUserService = onlineUserService;
    }

    @MessageMapping("/chat")
    @SendTo("/topic/chat")
    public Message sendMsg(Message msg) {
        //new feature
        //
        messageHistoryService.saveMessage(msg);
        //
        return msg;
    }


    @MessageMapping("/user")
    @SendTo("/topic/chat")
    public List<OnlineUser> newUser(@Payload Message message, SimpMessageHeaderAccessor headerAccessor) {
        headerAccessor.getSessionAttributes().put("sender", message.getSender());
        onlineUserService.addOnlineUser(message.getSender());
        return onlineUserService.getAllOnlineUsers();

    }
}
