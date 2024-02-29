package com.m.m.RealTimeChat.Controllers;


import com.m.m.RealTimeChat.Models.Message;
import com.m.m.RealTimeChat.Models.MessageHisrotyStorage;
import com.m.m.RealTimeChat.Models.OnlineUserStorage;
import com.m.m.RealTimeChat.Services.MessageHistoryService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;


@RestController
public class webSocketMessageControler {

    private final MessageHistoryService messageHistoryService;

    //Deprecated
    private final MessageHisrotyStorage storage;


    public webSocketMessageControler(MessageHisrotyStorage storage, OnlineUserStorage userStorage, MessageHistoryService messageHistoryService) {
        this.storage = storage;
        this.messageHistoryService = messageHistoryService;
    }

    @GetMapping("/history")
    @ResponseBody
    public List<Message> distributeMessageHistory() {
        //new
        return messageHistoryService.getPublicHistory();
        //
        //return storage.getMessageHistory();
    }


}
