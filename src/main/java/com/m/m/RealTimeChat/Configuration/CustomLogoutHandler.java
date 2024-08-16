package com.m.m.RealTimeChat.Configuration;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.m.m.RealTimeChat.Models.Message;
import com.m.m.RealTimeChat.Services.MessageHistoryService;
import com.m.m.RealTimeChat.Services.OnlineUserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class CustomLogoutHandler implements LogoutHandler {

    private final Object lock = new Object();

    @Autowired
    private OnlineUserService onlineUserService;

    @Autowired
    private MessageHistoryService messageHistoryService;

    private final SimpMessagingTemplate messagingTemplate;

    public CustomLogoutHandler(SimpMessagingTemplate messagingTemplate) {


        this.messagingTemplate = messagingTemplate;
    }

    @Override
    public void logout(HttpServletRequest request, HttpServletResponse response, Authentication authentication) {

        synchronized (lock) {

            /*System.out.println("DEBUG CUSTOM LOGOUT");*/
            if (authentication != null) {
                String user = authentication.getName();
                Map<String, String> message = new HashMap<>();
                message.put("type", "Leave");
                message.put("sender", user);
                message.put("sendTo", "public");
                message.put("date", LocalDateTime.now().format(DateTimeFormatter.ofPattern("d. M. yyyy HH:mm:ss")));
                message.put("content", user + " just left the chatroom");
                ObjectMapper mapper = new ObjectMapper();

                try {
                    String json = mapper.writeValueAsString(message);
                    Message disconnectMessage = mapper.readValue(json, Message.class);
                    messageHistoryService.saveMessage(disconnectMessage);
                } catch (JsonProcessingException e) {
                    throw new RuntimeException(e);
                }

                messagingTemplate.convertAndSend("/topic/chat", message);
                if (onlineUserService.findOnlineUser(user).isPresent()) {
                    onlineUserService.removeOnlineUser(user);
                }

            }

        }
    }
}

