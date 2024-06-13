package com.m.m.RealTimeChat.Controllers;


import com.m.m.RealTimeChat.Models.Message;
import com.m.m.RealTimeChat.Services.MessageHistoryService;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;


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

    @PutMapping("history/update")
    @ResponseBody
    public void updateMessageHistory(@RequestBody Map<String, String> request) {
        messageHistoryService.updateHistory(request.get("prevName"), request.get("actName"));
    }


}
