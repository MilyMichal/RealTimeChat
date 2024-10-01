package com.m.m.RealTimeChat.Controllers;


import com.m.m.RealTimeChat.Models.Message;
import com.m.m.RealTimeChat.Services.MessageHistoryService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
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
    @ResponseBody
    public List<Message> distributeMessageHistory() {
        return messageHistoryService.getFullPublicHistory();
    }

    @GetMapping("/public-latest")
    @ResponseBody
    public List<Message> distributeLatestMessageHistory() {
        return messageHistoryService.getLatestPublicHistory();
    }

    @PreAuthorize("@userStorage.validateRequestUser(authentication.name,#sender,#sendTo)")
    @GetMapping("/{sendTo}-{sender}")
    @ResponseBody
    public List<Message> distributeFullPrivateMessageHistory(@PathVariable String sendTo, @PathVariable String sender) {
        return messageHistoryService.getFullPrivateHistory(sendTo, sender);
    }

    @PreAuthorize("@userStorage.validateRequestUser(authentication.name,#sender,#sendTo)")
    @GetMapping("/{sendTo}-{sender}/latest")
    @ResponseBody
    public ResponseEntity<?> distributeLatestPrivateMessageHistory(@PathVariable String sendTo, @PathVariable String sender) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return messageHistoryService.getLatestPrivateHistory(sendTo, sender);
    }

    @PutMapping("/update")
    @ResponseBody
    public void updateMessageHistory(@RequestBody Map<String, String> request) {
        messageHistoryService.updateHistory(request.get("prevNick"), request.get("actNick"));
    }


}
