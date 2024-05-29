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
public class MessageHistoryController {

    private final MessageHistoryService messageHistoryService;


    public MessageHistoryController(MessageHistoryService messageHistoryService) {
        this.messageHistoryService = messageHistoryService;

    }

    @GetMapping("/history/public")
    @ResponseBody
    public List<Message> distributeMessageHistory() {
        return messageHistoryService.getFullPublicHistory();
    }

    @GetMapping("/history/public-latest")
    @ResponseBody
    public List<Message> distributeLatestMessageHistory() {
        return messageHistoryService.getLatestPublicHistory();
    }

    @GetMapping("/history/{sendTo}-{sender}")
    @ResponseBody
    public List<Message> distributeFullPrivateMessageHistory(@PathVariable String sendTo, @PathVariable String sender) {
        return messageHistoryService.getFullPrivateHistory(sendTo, sender);
    }

    @GetMapping("/history/{sendTo}-{sender}/latest")
    @ResponseBody
    public List<Message> distributeLatestPrivateMessageHistory(@PathVariable String sendTo, @PathVariable String sender) {
        return messageHistoryService.getLatestPrivateHistory(sendTo, sender);
    }



}
