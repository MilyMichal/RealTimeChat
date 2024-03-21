package com.m.m.RealTimeChat.Controllers;


import com.m.m.RealTimeChat.Models.Message;
import com.m.m.RealTimeChat.Models.OnlineUser;
import com.m.m.RealTimeChat.Services.MessageHistoryService;
import com.m.m.RealTimeChat.Services.OnlineUserService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;


@RestController
public class webSocketMessageController {

    private final MessageHistoryService messageHistoryService;
private final OnlineUserService onlineUserService;


    public webSocketMessageController(MessageHistoryService messageHistoryService, OnlineUserService onlineUserService) {
        this.messageHistoryService = messageHistoryService;
        this.onlineUserService = onlineUserService;
    }

    @GetMapping("/history/public")
    @ResponseBody
    public List<Message> distributeMessageHistory() {
        return messageHistoryService.getPublicHistory();
    }

    @GetMapping("/history/{sendTo}-{sender}")
    @ResponseBody
    public List<Message> distributePrivateMessageHistory(@PathVariable String sendTo,@PathVariable String sender) {
        return messageHistoryService.getPrivateHistory(sendTo,sender);
    }

    @GetMapping("/users")
    @ResponseBody
    public List<OnlineUser> getUsers() {
        return onlineUserService.getAllOnlineUsers();
    }

}
