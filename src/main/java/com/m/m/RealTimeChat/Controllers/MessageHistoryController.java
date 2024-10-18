package com.m.m.RealTimeChat.Controllers;


import com.m.m.RealTimeChat.Models.Message;
import com.m.m.RealTimeChat.Services.MessageHistoryService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@PreAuthorize("hasAnyRole('ADMIN','user')")
@RequestMapping("/history")
@RestController
public class MessageHistoryController {

    private final MessageHistoryService messageHistoryService;


    public MessageHistoryController(MessageHistoryService messageHistoryService) {
        this.messageHistoryService = messageHistoryService;

    }

    @GetMapping("/public")
   public List<Message> distributeMessageHistory() {
        return messageHistoryService.getFullPublicHistory();
    }

    @GetMapping("/public-latest")
    public List<Message> distributeLatestMessageHistory() {
        return messageHistoryService.getLatestPublicHistory();
    }

    @PreAuthorize("@userStorage.validateRequestUser(authentication.name,#sender,#sendTo)")
    @GetMapping("/{sendTo}-{sender}")
       public List<Message> distributeFullPrivateMessageHistory(@PathVariable String sendTo, @PathVariable String sender) {
        return messageHistoryService.getFullPrivateHistory(sendTo, sender);
    }

    @PreAuthorize("@userStorage.validateRequestUser(authentication.name,#sender,#sendTo)")
    @GetMapping("/{sendTo}-{sender}/latest")
     public ResponseEntity<List<Message>> distributeLatestPrivateMessageHistory(@PathVariable String sendTo, @PathVariable String sender) {
        return messageHistoryService.getLatestPrivateHistory(sendTo, sender);
    }

    @PutMapping("/update")
     public void updateMessageHistory(@RequestBody Map<String, String> request) {
        messageHistoryService.updateHistory(request.get("prevNick"), request.get("actNick"));
    }


}
