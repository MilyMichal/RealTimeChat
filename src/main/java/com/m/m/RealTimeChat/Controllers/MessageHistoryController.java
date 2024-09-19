package com.m.m.RealTimeChat.Controllers;


import com.m.m.RealTimeChat.Models.Message;
import com.m.m.RealTimeChat.Services.MessageHistoryService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
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

    @GetMapping("/{sendTo}-{sender}")
    @ResponseBody
    public List<Message> distributeFullPrivateMessageHistory(@PathVariable String sendTo, @PathVariable String sender) {
        return messageHistoryService.getFullPrivateHistory(sendTo, sender);
    }

    @GetMapping("/{sendTo}-{sender}/latest")
    @ResponseBody
    public ResponseEntity<?> distributeLatestPrivateMessageHistory(@PathVariable String sendTo, @PathVariable String sender, Principal principal) {
        return messageHistoryService.getLatestPrivateHistory(sendTo, sender, principal);
    }

    @PutMapping("/update")
    @ResponseBody
    public void updateMessageHistory(@RequestBody Map<String, String> request) {
        messageHistoryService.updateHistory(request.get("prevName"), request.get("actName"));
    }


}
